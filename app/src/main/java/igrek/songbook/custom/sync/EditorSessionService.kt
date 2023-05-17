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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import okhttp3.Request
import okhttp3.RequestBody

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
            remoteSongs.isEmpty() -> { // don't update local, push to remote. Local version is already latest.
                logger.info("Sync: empty remote")
                synchronizeStep4Push(session, localSongs, remoteSongs)
            }
            localChanged && remoteChanged -> {
                logger.info("Sync: merge conflict, local: $lastLocalHash -> $currentLocalHash, remote: $lastRemoteHash -> $currentRemoteHash")
                resolveConflicts(session, localSongs, remoteSongs)
            }
            localChanged -> { // don't update local, push to remote. Local version is already latest.
                logger.info("Sync: found local changes to push")
                synchronizeStep4Push(session, localSongs, remoteSongs)
            }
            remoteChanged -> {
                logger.info("Sync: fetching remote changes")
                applyRemoteChanges(session, localSongs, remoteSongs)
            }
            else -> {
                logger.info("Sync: local is up-to-date with the remote")
                synchronizeStep5SessionLink(session)
            }
        }
    }

    private suspend fun synchronizeStep4Push(session: EditorSessionDto, localSongs: List<CustomSong>, remoteSongs: List<EditorSongDto>) {
        logger.info("Sync: pushing local snapshot")
        val localIdToRemoteMap = songsRepository.customSongsDao.customSongs.syncSessionData.localIdToRemoteMap

        rememberNewHashes(session)

        val pushSongs: MutableList<EditorSongDto> = mutableListOf()
        localSongs.forEach { localSong: CustomSong ->
            val localId = localSong.id
            val remoteSongId = localIdToRemoteMap[localId] ?: run {
                val newId = deviceIdProvider.newUUID()
                localIdToRemoteMap[localId] = newId
                logger.debug("assigned new remote ID to local-remote song mapping: $localId -> $newId")
                newId
            }

            val pushSong = EditorSongDto.fromLocalSong(localSong, remoteSongId)
            pushSongs.add(pushSong)
        }

        val split = splitSets(localSongs, remoteSongs)

        val result = pushSongsAsync(session.id, pushSongs).await()
        result.fold(onSuccess = { updatedSession: EditorSessionDto ->
            logger.debug("Sync: local snapshot pushed: updated: ${split.differentCount}, added: ${split.remoteOnly.size}, deleted: ${split.localOnly.size}")
            rememberNewHashes(updatedSession) // update new remote hash after pushing
            synchronizeStep5SessionLink(updatedSession)
        }, onFailure = { e ->
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

    private fun rememberNewHashes(session: EditorSessionDto) {
        val localSongs = songsRepository.customSongsDao.customSongs.songs
        val currentLocalHash = SongHasher().hashLocalSongs(localSongs)
        val currentRemoteHash = session.current_hash
        songsRepository.customSongsDao.customSongs.syncSessionData.lastLocalHash = currentLocalHash
        songsRepository.customSongsDao.customSongs.syncSessionData.lastRemoteHash = currentRemoteHash
        logger.debug("Storing sync hashes: local: $currentLocalHash, remote: $currentRemoteHash")
    }

    private fun synchronizeStep5SessionLink(session: EditorSessionDto) {
        val editorLink = editorSessionUrl(session.id)
        uiInfoService.showInfoAction(R.string.sync_session_songs_synchronized, indefinite=false,
            actionResId=R.string.sync_copy_link) {
            clipboardManager.copyToSystemClipboard(editorLink)
            GlobalScope.launch {
                uiInfoService.clearSnackBars()
                uiInfoService.showInfo(R.string.sync_copy_link_copied)
            }
        }
    }

    private fun applyRemoteChanges(session: EditorSessionDto, localSongs: List<CustomSong>, remoteSongs: List<EditorSongDto>) {
        // in fact, it's force reset
        val split = splitSets(localSongs, remoteSongs)
        val localIdToRemoteMap = songsRepository.customSongsDao.customSongs.syncSessionData.localIdToRemoteMap
        split.common.forEach { (localOne, remoteOne) ->
            localIdToRemoteMap[localOne.id] = remoteOne.id
            if (syncedSongsDiffers(localOne, remoteOne)) {
                updateSongFromRemote(localOne, remoteOne)
            }
        }

        split.localOnly.forEach { localOne ->
            deleteSongFromRemote(localOne)
        }

        split.remoteOnly.forEach { remoteOne ->
            val localOne = createSongFromRemote(remoteOne)
            localIdToRemoteMap[localOne.id] = remoteOne.id
        }

        rememberNewHashes(session)
        logger.info("Sync: remote changes applied: updated: ${split.differentCount}, added: ${split.remoteOnly.size}, deleted: ${split.localOnly.size}")

        synchronizeStep5SessionLink(session)
    }

    private suspend fun softMergeRemoteChanges(session: EditorSessionDto, split: SetSplit, localSongs: List<CustomSong>, remoteSongs: List<EditorSongDto>) {
        val localIdToRemoteMap = songsRepository.customSongsDao.customSongs.syncSessionData.localIdToRemoteMap
        split.common.forEach { (localOne, remoteOne) ->
            localIdToRemoteMap[localOne.id] = remoteOne.id
            if (syncedSongsDiffers(localOne, remoteOne)) {
                updateSongFromRemote(localOne, remoteOne)
            }
        }

        split.remoteOnly.forEach { remoteOne ->
            val localOne = createSongFromRemote(remoteOne)
            localIdToRemoteMap[localOne.id] = remoteOne.id
        }

        rememberNewHashes(session)
        logger.info("Sync: remote changes merged: updated: ${split.differentCount}, added: ${split.remoteOnly.size}, kept back locally: ${split.localOnly.size}")

        synchronizeStep4Push(session, localSongs, remoteSongs)
    }

    private suspend fun resolveConflicts(session: EditorSessionDto, localSongs: List<CustomSong>, remoteSongs: List<EditorSongDto>) {
        val remoteUpdateTime = formatTimestampTime(session.update_timestamp)
        val localSongsCount = localSongs.size
        val remoteSongsCount = remoteSongs.size

        val split = splitSets(localSongs, remoteSongs)

        if (split.differentCount == 0) {
            logger.info("Sync: conflict can be softly resolved")
            softMergeRemoteChanges(session, split, localSongs, remoteSongs)
            return
        }

        val differentTitles = split.differentSongNames.joinToString("") { "\n  - $it" }
        val message = uiInfoService.resString(R.string.sync_session_conflict_summary,
            localSongsCount.toString(),
            remoteSongsCount.toString(),
            remoteUpdateTime,
            split.unchangedCount.toString(),
            split.differentCount.toString() + differentTitles,
            split.localOnly.size.toString(),
            split.remoteOnly.size.toString(),
        ).trimIndent().trim()
        logger.info("Sync: Conflict: $message")

        uiInfoService.dialogThreeChoices(
            titleResId = R.string.sync_session_conflict_detected,
            message = message,
            neutralButton = R.string.sync_session_conflict_take_local,
            neutralAction = {
                GlobalScope.launch {
                    safeExecute {
                        logger.info("Sync: Conflict: taking local")
                        synchronizeStep4Push(session, localSongs, remoteSongs)
                    }
                }
            },
            negativeButton = R.string.action_cancel,
            negativeAction = {},
            positiveButton = R.string.sync_session_conflict_take_remote,
            positiveAction = {
                GlobalScope.launch {
                    safeExecute {
                        logger.info("Sync: Conflict: taking remote")
                        applyRemoteChanges(session, localSongs, remoteSongs)
                    }
                }
            },
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

    private fun createSongFromRemote(remoteOne: EditorSongDto): CustomSong {
        val localSong = CustomSong(
            id = "0",
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
    ): Deferred<Result<EditorSessionDto>> {
        val dto = EditorSessionPushDto(songs = songs)
        val json =
            httpRequester.jsonSerializer.encodeToString(EditorSessionPushDto.serializer(), dto)
        val request: Request = Request.Builder()
            .url(pushSongsUrl(sessionId))
            .post(RequestBody.create(httpRequester.jsonType, json))
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

    private fun splitSets(
        localSongs: List<CustomSong>,
        remoteSongs: List<EditorSongDto>,
    ): SetSplit {
        val split = SetSplit()

        val localKeySelector = { it: CustomSong -> "${it.title} - ${it.categoryName}" }
        val remoteKeySelector = { it: EditorSongDto -> "${it.title} - ${it.artist}" }
        val localIdSelector = { it: CustomSong -> it.id }
        val remoteIdSelector = { it: EditorSongDto -> it.id }

        val localIdToRemoteMap = songsRepository.customSongsDao.customSongs.syncSessionData.localIdToRemoteMap

        val remoteSongsByKey: Map<String, EditorSongDto> = remoteSongs.associateBy { remoteKeySelector(it) }
        val remoteSongsById: MutableMap<String, EditorSongDto> = remoteSongs.associateBy { remoteIdSelector(it) }.toMutableMap()

        localSongs.forEach { localE ->
            val localKey = localKeySelector(localE)
            val remoteId = localIdToRemoteMap[localIdSelector(localE)]
            if (remoteId in remoteSongsById) {
                val remoteE = remoteSongsById[remoteId]!!
                val commonPair = localE to remoteE
                remoteSongsById.remove(remoteIdSelector(remoteE))
                split.common.add(commonPair)
            } else if (localKey in remoteSongsByKey) {
                val remoteE = remoteSongsByKey[localKey]!!
                val commonPair = localE to remoteE
                remoteSongsById.remove(remoteIdSelector(remoteE))
                split.common.add(commonPair)
            } else {
                split.localOnly.add(localE)
            }
        }

        remoteSongsById.values.forEach { remoteE ->
            split.remoteOnly.add(remoteE)
        }

        split.common.forEach { (localOne, remoteOne) ->
            if (syncedSongsDiffers(localOne, remoteOne)) {
                split.differentCount++
                split.differentSongNames.add(localOne.displayName())
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
        if (local.chordsNotationN.id != remote.chords_notation_id)
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

data class SetSplit(
    val common: MutableList<Pair<CustomSong, EditorSongDto>> = mutableListOf(),
    val localOnly: MutableList<CustomSong> = mutableListOf(),
    val remoteOnly: MutableList<EditorSongDto> = mutableListOf(),
    var differentCount: Int = 0,
    val differentSongNames: MutableList<String> = mutableListOf(),
) {
    val unchangedCount: Int get() = this.common.size - differentCount
}
