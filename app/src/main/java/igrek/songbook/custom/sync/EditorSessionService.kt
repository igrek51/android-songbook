package igrek.songbook.custom.sync

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.DeviceIdProvider
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.custom.CustomSong
import igrek.songbook.persistence.user.custom.SyncSessionData
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.system.ClipboardManager
import igrek.songbook.system.HttpRequester
import igrek.songbook.util.defaultScope
import igrek.songbook.util.ioScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class EditorSessionService(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    deviceIdProvider: LazyInject<DeviceIdProvider> = appFactory.deviceIdProvider,
    clipboardManager: LazyInject<ClipboardManager> = appFactory.clipboardManager,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val deviceIdProvider by LazyExtractor(deviceIdProvider)
    private val clipboardManager by LazyExtractor(clipboardManager)
    private val settingsState by LazyExtractor(appFactory.settingsState)

    private val logger: Logger = LoggerFactory.logger

    companion object {
        private const val songbookApiBase = "https://songbook.igrek.dev"
        private val editorSessionUrl =
            { sessionId: String -> "$songbookApiBase/ui/editor/session/$sessionId" }
    }

    private val httpRequester = HttpRequester()
    private val syncSessionData: SyncSessionData get() = songsRepository.customSongsDao.customSongs.syncSessionData
    private var quiet: Boolean = false

    fun synchronizeWithWeb(quiet: Boolean = false) {
        this.quiet = quiet
        ioScope.launch {
            try {
                uiInfoService.showInfo(R.string.sync_session_synchronizing, indefinite = true)
                synchronizeStep1CreateSession()
            } catch (e: Throwable) {
                UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
            }
        }
    }

    fun autoSyncCustomSongs() {
        if (settingsState.keepCustomSongsInSync) {
            synchronizeWithWeb(quiet = true)
        }
    }

    private suspend fun synchronizeStep1CreateSession() {
        val result = createSessionAsync().await()
        val sessionId: String = result.getOrThrow()
        synchronizeStep2Pull(sessionId)
    }

    private suspend fun synchronizeStep2Pull(sessionId: String) {
        val result = pullSyncHeaderAsync(sessionId).await()
        val header: EditorSyncHeaderDto = result.getOrThrow()
        synchronizeStep3Compare(header)
    }

    private suspend fun synchronizeStep3Compare(header: EditorSyncHeaderDto) {
        val sessionId = header.session_id
        val localSongs: List<CustomSong> = songsRepository.customSongsDao.customSongs.songs
        val remoteSongs: List<EditorSongHeaderDto> = header.song_headers
        val localPhantomSongs: MutableMap<String, Long> = syncSessionData.localTrash
        val remotePhantomSongs: List<EditorTrashSongDto> = header.deleted_songs
        val localIdToRemoteMap: MutableMap<String, String> = syncSessionData.localIdToRemoteMap

        val split = compareLocalAndRemote(sessionId, localSongs, remoteSongs, localPhantomSongs, remotePhantomSongs, localIdToRemoteMap)
        when {
            split.noChanges -> {
                logger.info("Sync: local is up-to-date with the remote")
            }
            split.noLocalChanges -> {
                logger.info("Sync: merging remote changes")
                synchronizeStep4Merge(split)
            }
            split.noRemoteChanges -> {
                logger.info("Sync: found local changes to push")
                synchronizeStep5Push(split)
            }
            else -> {
                logger.info("Sync: both local and remote changes - merge & push")
                synchronizeStep4Merge(split)
                synchronizeStep5Push(split)
            }
        }
        synchronizeStep6SessionLink(sessionId)
    }

    private fun synchronizeStep4Merge(split: SyncSplit) {
        // Merge changes coming from remote
        val newLocalRemoteIdMap = split.newLocalRemoteIdMap

        split.updatedRemotes.forEach { item: CrdtItemCommon ->
            updateSongByRemote(item.localValue, item.remoteValue)
        }

        split.addedRemotes.forEach { item: CrdtItemAddedRemote ->
            val localOne = createSongByRemote(item.remoteValue)
            newLocalRemoteIdMap[localOne.id] = item.remoteId
        }

        split.deletedRemotes.forEach { item: CrdtItemDeletedRemote ->
            deleteSongByRemote(item.localValue)
            newLocalRemoteIdMap.remove(item.localId)
        }

        // clear phantom local IDs, recreate local-remote mapping
        syncSessionData.localTrash.clear()
        syncSessionData.localIdToRemoteMap = newLocalRemoteIdMap

        logger.info("Sync: remote changes applied: updated: ${split.updatedRemotes.size}, added: ${split.addedRemotes.size}, deleted: ${split.deletedRemotes.size}")
    }

    private suspend fun synchronizeStep5Push(split: SyncSplit) {
        val localSongs: List<CustomSong> = songsRepository.customSongsDao.customSongs.songs
        val localIdToRemoteMap = syncSessionData.localIdToRemoteMap

        val pushSongs: MutableList<EditorSongDto> = mutableListOf()
        localSongs.forEach { localSong: CustomSong ->
            val localId = localSong.id
            var remoteSongId: String? = localIdToRemoteMap[localId]
            if (remoteSongId == null) {
                val newId = deviceIdProvider.newUUID()
                localIdToRemoteMap[localId] = newId
                remoteSongId = newId
                logger.debug("assigned new remote ID to local-remote song mapping: $localId -> $newId")
            }

            val pushSong = EditorSongDto.fromLocalSong(localSong, remoteSongId)
            pushSongs.add(pushSong)
        }

        val result = pushSongsAsync(split.sessionId, pushSongs).await()
        result.getOrThrow()
        logger.info("Sync: local snapshot pushed: updated: ${split.updatedLocals.size}, added: ${split.addedLocals.size}, deleted: ${split.deletedLocals.size}")
    }

    private fun synchronizeStep6SessionLink(sessionId: String) {
        if (this.quiet) {
            return uiInfoService.showInfo(R.string.sync_session_songs_synchronized_short)
        }
        val editorLink = editorSessionUrl(sessionId)
        uiInfoService.showInfoAction(R.string.sync_session_songs_synchronized,
            actionResId=R.string.sync_copy_link) {
            clipboardManager.copyToSystemClipboard(editorLink)
            defaultScope.launch {
                uiInfoService.clearSnackBars()
                uiInfoService.showInfo(R.string.sync_copy_link_copied)
            }
        }
    }


    private suspend fun compareLocalAndRemote(
        sessionId: String,
        localSongs: List<CustomSong>,
        remoteHeaders: List<EditorSongHeaderDto>,
        localPhantomSongs: MutableMap<String, Long>,
        remotePhantomSongs: List<EditorTrashSongDto>,
        localIdToRemoteMap: MutableMap<String, String>,
    ): SyncSplit {
        val localIdSelector = { it: CustomSong -> it.id }
        val remoteIdSelector = { it: EditorSongHeaderDto -> it.song_id }
        // remaining IDs to be matched
        val remoteHeadersById: MutableMap<String, EditorSongHeaderDto> = remoteHeaders
            .associateBy { remoteIdSelector(it) }.toMutableMap()
        val remotePhantomsById: Map<String, EditorTrashSongDto> = remotePhantomSongs
            .associateBy { it.song_id }.toMap()
        val remoteIdToLocalMap: Map<String, String> = localIdToRemoteMap.entries.associate{(k,v) -> v to k}

        val split = SyncSplit(sessionId = sessionId)
        val matches: MutableList<CrdtItemMatch> = mutableListOf()

        // existing local songs
        localSongs.forEach { localItem ->
            val localId: String = localIdSelector(localItem)
            val remoteId: String? = localIdToRemoteMap[localId]

            val match = CrdtItemMatch(
                localId = localId,
                localUpdateTime = localItem.updateTime / 1000, // millis to seconds
                localValue = localItem,
                localHash = SongHasher().stdSongContentHash(localItem),
            )

            when (remoteId) {
                null -> {} // unseen by remote - created by local
                in remoteHeadersById -> { // matched by ID
                    val remoteHeader = remoteHeadersById.getValue(remoteId)
                    remoteHeadersById.remove(remoteId) // don't process it again
                    // fetch remote song details
                    val remoteItem = pullSongAsync(sessionId, remoteId).await().getOrThrow()
                    match.remoteId = remoteId
                    match.remoteValue = remoteItem
                    match.remoteUpdateTime = remoteHeader.update_timestamp
                    match.remoteHash = remoteHeader.song_hash
                }
                in remotePhantomsById -> { // deleted by remote
                    val remotePhantom = remotePhantomsById.getValue(remoteId)
                    match.remoteId = remoteId
                    match.remoteValue = null
                    match.remoteUpdateTime = remotePhantom.delete_timestamp
                }
                else -> { // obsolete mapping entry
                    localIdToRemoteMap.remove(localId)
                }
            }
            matches.add(match)
        }

        // remaining remote songs
        remoteHeadersById.values.forEach { remoteHeader ->
            val remoteId = remoteHeader.song_id
            // fetch remote song details
            val remoteItem = pullSongAsync(sessionId, remoteId).await().getOrThrow()
            val match = CrdtItemMatch(
                remoteId = remoteId,
                remoteUpdateTime = remoteHeader.update_timestamp,
                remoteValue = remoteItem,
                remoteHash = remoteHeader.song_hash,
            )
            when (val localId: String? = remoteIdToLocalMap[remoteId]) {
                null -> {} // unseen by local - created by remote
                in localPhantomSongs -> { // deleted by local
                    val localPhantom = localPhantomSongs.getValue(localId)
                    match.localId = localId
                    match.localValue = null
                    match.localUpdateTime = localPhantom
                }
            }
            matches.add(match)
        }

        matches.forEach { match ->
            val localUpdateTimeN = match.localUpdateTime ?: 0
            val remoteUpdateTimeN = match.remoteUpdateTime ?: 0
            val localId = match.localId
            val remoteId = match.remoteId
            if (localId != null && remoteId != null) {
                split.newLocalRemoteIdMap[localId] = remoteId
            }
            when {
                localId == null && remoteId != null -> split.addedRemotes.add(CrdtItemAddedRemote(
                    remoteId = remoteId,
                    remoteUpdateTime = remoteUpdateTimeN,
                    remoteValue = match.remoteValue!!,
                    remoteHash = match.remoteHash!!,
                ))
                remoteId == null && localId != null -> split.addedLocals.add(CrdtItemAddedLocal(
                    localId = localId,
                    localUpdateTime = localUpdateTimeN,
                    localValue = match.localValue!!,
                    localHash = match.localHash!!,
                ))
                localId == null && remoteId == null -> throw RuntimeException("both local and remote is empty")
                match.localHash == match.remoteHash -> split.common.add(CrdtItemCommon(
                    localId = localId!!,
                    localUpdateTime = localUpdateTimeN,
                    localValue = match.localValue!!,
                    localHash = match.localHash!!,
                    remoteId = remoteId!!,
                    remoteUpdateTime = remoteUpdateTimeN,
                    remoteValue = match.remoteValue!!,
                    remoteHash = match.remoteHash!!,
                ))
                // resolve conflict - last write wins
                localUpdateTimeN >= remoteUpdateTimeN -> { // local wins
                    if (match.localValue == null) {
                        split.deletedLocals.add(CrdtItemDeletedLocal(
                            localId = localId!!,
                            localUpdateTime = localUpdateTimeN,
                            remoteId = remoteId!!,
                            remoteUpdateTime = remoteUpdateTimeN,
                        ))
                    } else if (match.remoteValue == null) {
                        split.addedLocals.add(CrdtItemAddedLocal(
                            localId = localId!!,
                            localUpdateTime = localUpdateTimeN,
                            localValue = match.localValue!!,
                            localHash = match.localHash!!,
                        ))
                    } else {
                        split.updatedLocals.add(CrdtItemCommon(
                            localId = localId!!,
                            localUpdateTime = localUpdateTimeN,
                            localValue = match.localValue!!,
                            localHash = match.localHash!!,
                            remoteId = remoteId!!,
                            remoteUpdateTime = remoteUpdateTimeN,
                            remoteValue = match.remoteValue!!,
                            remoteHash = match.remoteHash!!,
                        ))
                    }
                }
                localUpdateTimeN < remoteUpdateTimeN -> {
                    if (match.remoteValue == null) {
                        split.deletedRemotes.add(CrdtItemDeletedRemote(
                            localId = localId!!,
                            localUpdateTime = localUpdateTimeN,
                            localValue = match.localValue!!,
                            remoteId = remoteId!!,
                            remoteUpdateTime = remoteUpdateTimeN,
                        ))
                    } else if (match.localValue == null) {
                        split.addedRemotes.add(CrdtItemAddedRemote(
                            remoteId = remoteId!!,
                            remoteUpdateTime = remoteUpdateTimeN,
                            remoteValue = match.remoteValue!!,
                            remoteHash = match.remoteHash!!,
                        ))
                    } else {
                        split.updatedRemotes.add(CrdtItemCommon(
                            localId = localId!!,
                            localUpdateTime = localUpdateTimeN,
                            localValue = match.localValue!!,
                            localHash = match.localHash!!,
                            remoteId = remoteId!!,
                            remoteUpdateTime = remoteUpdateTimeN,
                            remoteValue = match.remoteValue!!,
                            remoteHash = match.remoteHash!!,
                        ))
                    }
                }
                else -> throw RuntimeException("unknown synchronization status")
            }
        }

        logger.debug("Sync: Compare: locally updated: ${split.updatedLocals.size}, locally added: ${split.addedLocals.size}, locally deleted: ${split.deletedLocals.size}, " +
            "remotely updated: ${split.updatedRemotes.size}, remotely added: ${split.addedRemotes.size}, remotely deleted: ${split.deletedRemotes.size}")
        return split
    }

    private fun updateSongByRemote(localOne: CustomSong, remoteOne: EditorSongDto) {
        localOne.title = remoteOne.title
        localOne.content = remoteOne.content
        localOne.categoryName = remoteOne.artist
        localOne.updateTime = remoteOne.update_timestamp * 1000 // seconds to millis
        localOne.chordsNotation = ChordsNotation.mustParseById(remoteOne.chords_notation_id)
        songsRepository.customSongsDao.saveCustomSong(localOne)
    }

    private fun createSongByRemote(remoteOne: EditorSongDto): CustomSong {
        val localSong = CustomSong(
            id = remoteOne.id,
            title = remoteOne.title,
            categoryName = remoteOne.artist,
            content = remoteOne.content,
            versionNumber = 1,
            createTime = remoteOne.update_timestamp * 1000, // seconds to millis
            updateTime = remoteOne.update_timestamp * 1000, // seconds to millis
            chordsNotation = ChordsNotation.mustParseById(remoteOne.chords_notation_id),
        )
        songsRepository.customSongsDao.saveCustomSong(localSong)
        return localSong
    }

    private fun deleteSongByRemote(localOne: CustomSong) {
        songsRepository.customSongsDao.removeCustomSong(localOne)
    }


    private fun createSessionAsync(): Deferred<Result<String>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val dto = EditorSessionCreateDto(device_id = deviceId)
        val json =
            httpRequester.jsonSerializer.encodeToString(EditorSessionCreateDto.serializer(), dto)
        val request: Request = Request.Builder()
            .url("$songbookApiBase/api/editor/session")
            .post(json.toRequestBody(httpRequester.jsonType))
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body?.string() ?: ""
            val responseData: EditorSessionCreatedDto =
                httpRequester.jsonSerializer.decodeFromString(
                    EditorSessionCreatedDto.serializer(),
                    jsonData
                )
            responseData.id
        }
    }

    private fun pullAllSongsAsync(sessionId: String): Deferred<Result<EditorSessionDto>> {
        val request: Request = Request.Builder()
            .url("$songbookApiBase/api/editor/session/$sessionId")
            .get()
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body?.string() ?: ""
            val responseData: EditorSessionDto = httpRequester.jsonSerializer.decodeFromString(
                EditorSessionDto.serializer(),
                jsonData
            )
            responseData
        }
    }

    private fun pullSongAsync(sessionId: String, songId: String): Deferred<Result<EditorSongDto>> {
        val request: Request = Request.Builder()
            .url("$songbookApiBase/api/editor/session/$sessionId/song/$songId")
            .get()
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body?.string() ?: ""
            val responseData: EditorSongDto = httpRequester.jsonSerializer.decodeFromString(
                EditorSongDto.serializer(),
                jsonData
            )
            responseData
        }
    }

    private fun pullSyncHeaderAsync(sessionId: String): Deferred<Result<EditorSyncHeaderDto>> {
        val request: Request = Request.Builder()
            .url("$songbookApiBase/api/editor/session/$sessionId/header")
            .get()
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body?.string() ?: ""
            val responseData: EditorSyncHeaderDto = httpRequester.jsonSerializer.decodeFromString(
                EditorSyncHeaderDto.serializer(),
                jsonData
            )
            responseData
        }
    }

    private fun pushSongsAsync(
        sessionId: String,
        songs: List<EditorSongDto>,
    ): Deferred<Result<Unit>> {
        val dto = EditorSessionPushDto(songs = songs)
        val json =
            httpRequester.jsonSerializer.encodeToString(EditorSessionPushDto.serializer(), dto)
        val request: Request = Request.Builder()
            .url("$songbookApiBase/api/editor/session/$sessionId/push")
            .post(json.toRequestBody(httpRequester.jsonType))
            .build()
        return httpRequester.httpRequestAsync(request) {}
    }
}

@Serializable
data class EditorSessionCreateDto(
    var device_id: String,
)

@Serializable
data class EditorSessionCreatedDto(
    var id: String,
    var create_timestamp: Long, // timestamp in seconds
    var update_timestamp: Long, // timestamp in seconds
)

@Serializable
data class EditorSongDto(
    var id: String,
    var update_timestamp: Long, // timestamp in seconds
    var title: String,
    var artist: String?,
    var content: String,
    var chords_notation_id: Long,
) {
    companion object {
        fun fromLocalSong(local: CustomSong, remoteSongId: String): EditorSongDto = EditorSongDto(
            id = remoteSongId,
            update_timestamp = local.updateTime / 1000, // millis to seconds
            title = local.title,
            artist = local.categoryName,
            content = local.content,
            chords_notation_id = local.chordsNotationN.id,
        )
    }
}

@Serializable
data class EditorSessionDto(
    var id: String,
    var create_timestamp: Long,
    var update_timestamp: Long,
    var current_hash: String,
    var songs: List<EditorSongDto>,
)

@Serializable
data class EditorSessionPushDto(
    var songs: List<EditorSongDto>,
)

@Serializable
data class EditorSyncHeaderDto(
    var session_id: String,
    var create_timestamp: Long,
    var update_timestamp: Long,
    var session_hash: String,
    var song_headers: List<EditorSongHeaderDto>,
    var deleted_songs: List<EditorTrashSongDto>,
)

@Serializable
data class EditorSongHeaderDto(
    var song_id: String,
    var song_hash: String,
    var update_timestamp: Long,
)

@Serializable
data class EditorTrashSongDto(
    var song_id: String,
    var delete_timestamp: Long,
)


data class SyncSplit(
    val sessionId: String,
    val addedLocals: MutableList<CrdtItemAddedLocal> = mutableListOf(), // added locally, missing remotely
    val deletedLocals: MutableList<CrdtItemDeletedLocal> = mutableListOf(), // deleted by local
    val updatedLocals: MutableList<CrdtItemCommon> = mutableListOf(), // local is newer
    val addedRemotes: MutableList<CrdtItemAddedRemote> = mutableListOf(), // added remotely, missing locally
    val deletedRemotes: MutableList<CrdtItemDeletedRemote> = mutableListOf(), // deleted by remote
    val updatedRemotes: MutableList<CrdtItemCommon> = mutableListOf(), // remote is newer
    val common: MutableList<CrdtItemCommon> = mutableListOf(),
    val newLocalRemoteIdMap: MutableMap<String, String> = mutableMapOf(),
) {
    val noChanges: Boolean get() = this.noLocalChanges && this.noRemoteChanges

    val noLocalChanges: Boolean get() {
        return this.addedLocals.isEmpty()
                && this.deletedLocals.isEmpty()
                && this.updatedLocals.isEmpty()
    }

    val noRemoteChanges: Boolean get() {
        return this.addedRemotes.isEmpty()
                && this.deletedRemotes.isEmpty()
                && this.updatedRemotes.isEmpty()
    }
}

data class CrdtItemMatch(
    var localId: String? = null,
    var localUpdateTime: Long? = null,
    var localValue: CustomSong? = null, // null + non-empty ID is a deleted phantom
    var localHash: String? = null,
    var remoteId: String? = null,
    var remoteUpdateTime: Long? = null,
    var remoteValue: EditorSongDto? = null, // null + non-empty ID is a deleted phantom
    var remoteHash: String? = null,
)

data class CrdtItemAddedLocal(
    var localId: String,
    var localUpdateTime: Long,
    var localValue: CustomSong,
    var localHash: String,
)

data class CrdtItemAddedRemote(
    var remoteId: String,
    var remoteUpdateTime: Long,
    var remoteValue: EditorSongDto,
    var remoteHash: String,
)

data class CrdtItemDeletedLocal(
    var localId: String,
    var localUpdateTime: Long,
    var remoteId: String,
    var remoteUpdateTime: Long,
)

data class CrdtItemDeletedRemote(
    var localId: String,
    var localUpdateTime: Long,
    var localValue: CustomSong,
    var remoteId: String,
    var remoteUpdateTime: Long,
)

data class CrdtItemCommon(
    var localId: String,
    var localUpdateTime: Long,
    var localValue: CustomSong,
    var localHash: String,
    var remoteId: String,
    var remoteUpdateTime: Long,
    var remoteValue: EditorSongDto,
    var remoteHash: String,
)
