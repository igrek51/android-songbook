package igrek.songbook.custom.sync

import igrek.songbook.R
import igrek.songbook.admin.HttpRequester
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.DeviceIdProvider
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.custom.CustomSong
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.system.ClipboardManager
import igrek.songbook.util.formatTimestampTime
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import okhttp3.Request
import okhttp3.RequestBody

@OptIn(DelicateCoroutinesApi::class)
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

    private val logger: Logger = LoggerFactory.logger

    companion object {
        private const val songbookApiBase = "https://songbook.igrek.dev"
        private const val createSessionUrl = "$songbookApiBase/api/editor/session"
        private val pullSongsUrl =
            { session: String -> "$songbookApiBase/api/editor/session/$session" }
        private val pushSongsUrl =
            { session: String -> "$songbookApiBase/api/editor/session/$session" }
        private val editorSessionUrl =
            { session: String -> "$songbookApiBase/ui/editor/session/$session" }
    }

    private val httpRequester = HttpRequester()

    fun synchronizeWithWeb() {
        GlobalScope.launch(Dispatchers.IO) {
            safeExecute {
                uiInfoService.showInfo(R.string.sync_session_synchronizing)
                synchronizeStep1CreateSession()
            }
        }
    }

    private suspend fun synchronizeStep1CreateSession() {
        val result = createSessionAsync().await()
        result.fold(onSuccess = { sessionId: String ->
            synchronizeStep2Pull(sessionId)
        }, onFailure = { e ->
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

    private suspend fun synchronizeStep2Pull(sessionId: String) {
        val result = pullSongsAsync(sessionId).await()
        result.fold(onSuccess = { session: EditorSessionDto ->
            synchronizeStep3Merge(session)
        }, onFailure = { e ->
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

    private suspend fun synchronizeStep3Merge(session: EditorSessionDto) {
        val localSongs: List<CustomSong> = songsRepository.customSongsDao.customSongs.songs
        val remoteSongs: List<EditorSongDto> = session.songs
        val currentLocalHash = SongHasher().hashLocalSongs(localSongs)
        val currentRemoteHash = session.current_hash
        val lastLocalHash = songsRepository.customSongsDao.customSongs.syncSessionData.lastLocalHash
        val lastRemoteHash = songsRepository.customSongsDao.customSongs.syncSessionData.lastRemoteHash

        val localChanged = currentLocalHash != lastLocalHash
        val remoteChanged = currentRemoteHash != lastRemoteHash

        when {
            remoteSongs.isEmpty() -> { // do nothing. Local version is already latest.
                logger.info("Sync: empty remote")
            }
            localChanged && remoteChanged -> {
                logger.info("Sync: merge conflict")
                resolveConflicts(session, localSongs, remoteSongs)
                return
            }
            localChanged -> { // do nothing. Local version is already latest.
                logger.info("Sync: found local changes to push")
            }
            remoteChanged -> {
                logger.info("Sync: merging remote changes")
                mergeRemoteChanges(localSongs, remoteSongs)
            }
            else -> {
                logger.info("Sync: local is up-to-date with the remote")
            }
        }

        synchronizeStep4Push(session)
    }

    private suspend fun synchronizeStep4Push(session: EditorSessionDto) {
        val localSongs = songsRepository.customSongsDao.customSongs.songs
        val localIdToRemoteMap = songsRepository.customSongsDao.customSongs.syncSessionData.localIdToRemoteMap

        val currentLocalHash = SongHasher().hashLocalSongs(localSongs)
        val currentRemoteHash = session.current_hash
        songsRepository.customSongsDao.customSongs.syncSessionData.lastLocalHash = currentLocalHash
        songsRepository.customSongsDao.customSongs.syncSessionData.lastRemoteHash = currentRemoteHash

        val pushSongs: MutableList<EditorSongDto> = mutableListOf()
        localSongs.forEach { localSong: CustomSong ->
            val localId = localSong.id.toString()
            val remoteSongId = localIdToRemoteMap[localId] ?: deviceIdProvider.newUUID()
            localIdToRemoteMap[localId] = remoteSongId

            val pushSong = EditorSongDto.fromLocalSong(localSong, remoteSongId)
            pushSongs.add(pushSong)
        }

        val editorLink = editorSessionUrl(session.id)
        val result = pushSongsAsync(session.id, pushSongs).await()
        result.fold(onSuccess = {
            uiInfoService.showInfoAction(R.string.sync_session_songs_synchronized, indefinite=true,
                actionResId=R.string.sync_copy_link) {
                clipboardManager.copyToSystemClipboard(editorLink)
                GlobalScope.launch {
                    uiInfoService.clearSnackBars()
                    uiInfoService.showInfo(R.string.sync_copy_link_copied)
                }
            }
        }, onFailure = { e ->
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

    private fun mergeRemoteChanges(localSongs: List<CustomSong>, remoteSongs: List<EditorSongDto>) {
        // in fact, it's force reset
        val localIdToRemoteMap = songsRepository.customSongsDao.customSongs.syncSessionData.localIdToRemoteMap
        val split = splitSets(localSongs, remoteSongs)
        var updated = 0
        split.common.forEach { (localOne, remoteOne) ->
            localIdToRemoteMap[localOne.id.toString()] = remoteOne.id
            if (syncedSongsDiffers(localOne, remoteOne)) {
                updateSongFromRemote(localOne, remoteOne)
                updated++
            }
        }

        split.localOnly.forEach { localOne ->
            deleteSongFromRemote(localOne)
        }

        split.remoteOnly.forEach { remoteOne ->
            createSongFromRemote(remoteOne)
        }

        logger.info("Sync: remote changes merged: updated: $updated, added: ${split.remoteOnly.size}, deleted: ${split.localOnly.size}")
    }

    private suspend fun resolveConflicts(session: EditorSessionDto, localSongs: List<CustomSong>, remoteSongs: List<EditorSongDto>) {
        val remoteUpdateTime = formatTimestampTime(session.update_timestamp)
        val localSongsCount = localSongs.size
        val remoteSongsCount = remoteSongs.size
        val message = """
        There is a conflict found between local songs and the remote ones.
        During the synchronization session, there was changes applied on both sides.
        
        Please choose what version is the right one.
        WARNING: The other one will get overwritten!
        
        Local (on this device): $localSongsCount songs
        Remote (Web Editor on the server): $remoteSongsCount songs, updated at $remoteUpdateTime
        """.trimIndent()

        uiInfoService.dialogThreeChoices(
            titleResId = R.string.sync_session_conflict_detected,
            message = message,
            positiveButton = R.string.sync_session_conflict_take_local,
            positiveAction = {
                GlobalScope.launch {
                    safeExecute {
                        logger.info("Sync: Conflict: taking local")
                        synchronizeStep4Push(session)
                    }
                }
            },
            negativeButton = R.string.sync_session_conflict_take_remote,
            negativeAction = {
                GlobalScope.launch {
                    safeExecute {
                        logger.info("Sync: Conflict: taking remote")
                        mergeRemoteChanges(localSongs, remoteSongs)
                        synchronizeStep4Push(session)
                    }
                }
            },
            neutralButton = R.string.action_cancel,
            neutralAction = {},
        )
    }

    private fun updateSongFromRemote(localOne: CustomSong, remoteOne: EditorSongDto) {
        localOne.title = remoteOne.title
        localOne.content = remoteOne.content
        localOne.categoryName = remoteOne.artist
        localOne.updateTime = remoteOne.update_timestamp * 1000 // seconds to millis
        localOne.chordsNotation = ChordsNotation.mustParseById(remoteOne.chords_notation_id)

        songsRepository.customSongsDao.saveCustomSong(localOne)
    }

    private fun createSongFromRemote(remoteOne: EditorSongDto) {
        val localSong = CustomSong(
            id = 0,
            title = remoteOne.title,
            categoryName = remoteOne.artist,
            content = remoteOne.content,
            versionNumber = 1,
            createTime = remoteOne.update_timestamp * 1000, // seconds to millis
            updateTime = remoteOne.update_timestamp * 1000, // seconds to millis
            chordsNotation = ChordsNotation.mustParseById(remoteOne.chords_notation_id),
        )
        songsRepository.customSongsDao.saveCustomSong(localSong)
    }

    private fun deleteSongFromRemote(localOne: CustomSong) {
        songsRepository.customSongsDao.removeCustomSong(localOne)
    }

    private fun createSessionAsync(): Deferred<Result<String>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val dto = EditorSessionCreateDto(device_id = deviceId)
        val json =
            httpRequester.jsonSerializer.encodeToString(EditorSessionCreateDto.serializer(), dto)
        val request: Request = Request.Builder()
            .url(createSessionUrl)
            .post(RequestBody.create(httpRequester.jsonType, json))
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body()?.string() ?: ""
            val responseData: EditorSessionCreatedDto =
                httpRequester.jsonSerializer.decodeFromString(
                    EditorSessionCreatedDto.serializer(),
                    jsonData
                )
            responseData.id
        }
    }

    private fun pullSongsAsync(sessionId: String): Deferred<Result<EditorSessionDto>> {
        val request: Request = Request.Builder()
            .url(pullSongsUrl(sessionId))
            .get()
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body()?.string() ?: ""
            val responseData: EditorSessionDto = httpRequester.jsonSerializer.decodeFromString(
                EditorSessionDto.serializer(),
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
            .url(pushSongsUrl(sessionId))
            .post(RequestBody.create(httpRequester.jsonType, json))
            .build()
        return httpRequester.httpRequestAsync(request) { }
    }

    private fun splitSets(
        localSongs: List<CustomSong>,
        remoteSongs: List<EditorSongDto>
    ): SetSplit {
        val split = SetSplit()

        val localKeySelector = { it: CustomSong -> "${it.title} - ${it.categoryName}" }
        val remoteKeySelector = { it: EditorSongDto -> "${it.title} - ${it.artist}" }

        val localSongsById: Map<String, CustomSong> =
            localSongs.associateBy { localKeySelector(it) }
        val remoteSongsById: Map<String, EditorSongDto> =
            remoteSongs.associateBy { remoteKeySelector(it) }

        localSongs.forEach { localE ->
            val id = localKeySelector(localE)
            if (id in remoteSongsById) {
                val commonPair = localE to remoteSongsById[id]!!
                split.common.add(commonPair)
            } else {
                split.localOnly.add(localE)
            }
        }

        remoteSongs.forEach { remoteE ->
            val id = remoteKeySelector(remoteE)
            if (id !in localSongsById) {
                split.remoteOnly.add(remoteE)
            }
        }

        return split
    }

    private fun syncedSongsDiffers(local: CustomSong, remote: EditorSongDto): Boolean {
        if (local.title != remote.title)
            return true
        if (local.categoryName.orEmpty() != remote.artist.orEmpty())
            return true
        if (local.content != remote.content)
            return true
        if (local.chordsNotation.id != remote.chords_notation_id)
            return true
        return false
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
            update_timestamp = local.updateTime / 1000,
            title = local.title,
            artist = local.categoryName,
            content = local.content,
            chords_notation_id = local.chordsNotation.id,
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

data class SetSplit(
    val common: MutableList<Pair<CustomSong, EditorSongDto>> = mutableListOf(),
    val localOnly: MutableList<CustomSong> = mutableListOf(),
    val remoteOnly: MutableList<EditorSongDto> = mutableListOf(),
)
