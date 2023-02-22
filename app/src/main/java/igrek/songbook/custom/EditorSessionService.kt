package igrek.songbook.custom

import igrek.songbook.R
import igrek.songbook.admin.HttpRequester
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.DeviceIdProvider
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.custom.CustomSong
import igrek.songbook.settings.preferences.PreferencesState
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import okhttp3.Request
import okhttp3.RequestBody

@OptIn(DelicateCoroutinesApi::class)
class EditorSessionService (
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    editSongLayoutController: LazyInject<EditSongLayoutController> = appFactory.editSongLayoutController,
    exportFileChooser: LazyInject<ExportFileChooser> = appFactory.exportFileChooser,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
    deviceIdProvider: LazyInject<DeviceIdProvider> = appFactory.deviceIdProvider,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val layoutController by LazyExtractor(layoutController)
    private val editSongLayoutController by LazyExtractor(editSongLayoutController)
    private val exportFileChooser by LazyExtractor(exportFileChooser)
    private val preferencesState by LazyExtractor(preferencesState)
    private val deviceIdProvider by LazyExtractor(deviceIdProvider)

    companion object {
        private const val songbookApiBase = "https://songbook.igrek.dev"
        private const val createSessionUrl = "$songbookApiBase/api/editor/session"
        private val pullSongsUrl = { session: String -> "$songbookApiBase/api/editor/session/$session" }
        private val pushSongsUrl = { session: String -> "$songbookApiBase/api/editor/session/$session" }
    }

    private val httpRequester = HttpRequester()

    fun syncrhonizeWithWeb() {
        GlobalScope.launch(Dispatchers.IO) {
            val result = createSessionAsync().await()
            result.fold(onSuccess = { sessionId: String ->
                syncrhonizeStep2Pull(sessionId)
            }, onFailure = { e ->
                UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
            })
        }
    }

    private suspend fun syncrhonizeStep2Pull(sessionId: String) {
        val result = pullSongsAsync(sessionId).await()
        result.fold(onSuccess = { session: EditorSessionDto ->
            syncrhonizeStep3Merge(session)
        }, onFailure = { e ->
            UiErrorHandler().handleError(e, R.string.error_communication_breakdown)
        })
    }

    private fun syncrhonizeStep3Merge(session: EditorSessionDto) {
        val localSongs = songsRepository.customSongsDao.customSongs.songs
        val remoteSongs = session.songs

        val split = splitSets(localSongs, remoteSongs)

        split.common.forEach { (localOne, remoteOne) ->

        }

    }

    private fun createSessionAsync(): Deferred<Result<String>> {
        val deviceId = deviceIdProvider.getDeviceId()
        val dto = EditorSessionCreateDto(device_id=deviceId)
        val json = httpRequester.jsonSerializer.encodeToString(EditorSessionCreateDto.serializer(), dto)
        val request: Request = Request.Builder()
            .url(createSessionUrl)
            .post(RequestBody.create(httpRequester.jsonType, json))
            .build()
        return httpRequester.httpRequestAsync(request) { response ->
            val jsonData = response.body()?.string() ?: ""
            val responseData: EditorSessionCreatedDto = httpRequester.jsonSerializer.decodeFromString(EditorSessionCreatedDto.serializer(), jsonData)
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
            val responseData: EditorSessionDto = httpRequester.jsonSerializer.decodeFromString(EditorSessionDto.serializer(), jsonData)
            responseData
        }
    }

    private fun pushSongsAsync(sessionId: String, songs: List<EditorSongDto>): Deferred<Result<Unit>> {
        val dto = EditorSessionPushDto(songs=songs)
        val json = httpRequester.jsonSerializer.encodeToString(EditorSessionPushDto.serializer(), dto)
        val request: Request = Request.Builder()
            .url(pushSongsUrl(sessionId))
            .post(RequestBody.create(httpRequester.jsonType, json))
            .build()
        return httpRequester.httpRequestAsync(request) { }
    }

    private fun splitSets(localSongs: List<CustomSong>, remoteSongs: List<EditorSongDto>): SetSplit {
        val split = SetSplit()

        val localKeySelector = { it: CustomSong -> "${it.title} - ${it.categoryName}" }
        val remoteKeySelector = { it: EditorSongDto -> "${it.title} - ${it.artist}" }

        val localSongsById: Map<String, CustomSong> = localSongs.associateBy { localKeySelector(it) }
        val remoteSongsById: Map<String, EditorSongDto> = remoteSongs.associateBy { remoteKeySelector(it) }

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

    private fun syncElementsDiffers(local: CustomSong, remote: EditorSongDto): Boolean {
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
    var create_timestamp: Long,
    var update_timestamp: Long,
)

@Serializable
data class EditorSongDto(
    var id: String,
    var update_timestamp: Long,
    var title: String,
    var artist: String?,
    var content: String,
    var chords_notation_id: Long,
)

@Serializable
data class EditorSessionDto(
    var id: String,
    var create_timestamp: Long,
    var update_timestamp: Long,
    var current_hash: String,
    var known_hashes: List<String>,
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
