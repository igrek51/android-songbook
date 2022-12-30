package igrek.songbook.admin.antechamber


import igrek.songbook.R
import igrek.songbook.admin.AdminService
import igrek.songbook.admin.HttpRequester
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.send.SongLanguageDetector
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.util.*

@OptIn(DelicateCoroutinesApi::class)
class AntechamberService(
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    adminService: LazyInject<AdminService> = appFactory.adminService,
    adminSongsLayoutContoller: LazyInject<AdminSongsLayoutContoller> = appFactory.adminSongsLayoutContoller,
) {
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val adminService by LazyExtractor(adminService)
    private val adminSongsLayoutContoller by LazyExtractor(adminSongsLayoutContoller)

    companion object {
        private const val antechamberApiBase = "https://antechamber.chords.igrek.dev/api/v4"
        private const val chordsApiBase = "https://chords.igrek.dev/api/v5"

        private const val allSongsUrl = "$antechamberApiBase/songs"
        private val specificSongUrl = { id: Long -> "$antechamberApiBase/songs/$id" }
        private const val approveSongUrl = "$chordsApiBase/songs"
        private val updatePublicSongIdUrl = { id: Long -> "$chordsApiBase/songs/$id" }

        private const val authTokenHeader = "X-Auth-Token"
    }

    private val httpRequester = HttpRequester()
    private val jsonType = MediaType.parse("application/json; charset=utf-8")
    private val jsonSerializer = Json {
        encodeDefaults = true
        ignoreUnknownKeys = false
        isLenient = false
        allowStructuredMapKeys = true
        prettyPrint = false
        useArrayPolymorphism = false
    }

    fun downloadSongsAsync(): Deferred<Result<List<Song>>> {
        val request: Request = Request.Builder()
            .url(allSongsUrl)
            .addHeader(authTokenHeader, adminService.userAuthToken)
            .build()
        return httpRequester.httpRequestAsync(request) { response: Response ->
            val json = response.body()?.string() ?: ""
            val allDtos: AllAntechamberSongsDto =
                jsonSerializer.decodeFromString(AllAntechamberSongsDto.serializer(), json)
            val antechamberSongs: List<Song> = allDtos.toModel()
            antechamberSongs
        }
    }

    private fun updateAntechamberSongAsync(song: Song): Deferred<Result<Unit>> {
        val antechamberSongDto = AntechamberSongDto.fromModel(song)
        val json =
            jsonSerializer.encodeToString(AntechamberSongDto.serializer(), antechamberSongDto)
        val request: Request = Request.Builder()
            .url(specificSongUrl(song.id))
            .put(RequestBody.create(jsonType, json))
            .addHeader(authTokenHeader, adminService.userAuthToken)
            .build()
        return httpRequester.httpRequestAsync(request) { }
    }

    fun createAntechamberSongAsync(song: Song): Deferred<Result<Unit>> {
        logger.info("Sending new antechamber song")
        val antechamberSongDto = AntechamberSongDto.fromModel(song)
        antechamberSongDto.id = null
        val json =
            jsonSerializer.encodeToString(AntechamberSongDto.serializer(), antechamberSongDto)
        val request: Request = Request.Builder()
            .url(allSongsUrl)
            .post(RequestBody.create(jsonType, json))
            .build()
        return httpRequester.httpRequestAsync(request) { }
    }

    fun deleteAntechamberSongAsync(song: Song): Deferred<Result<Unit>> {
        val request: Request = Request.Builder()
            .url(specificSongUrl(song.id))
            .delete()
            .addHeader(authTokenHeader, adminService.userAuthToken)
            .build()
        return httpRequester.httpRequestAsync(request) { }
    }

    fun updatePublicSongAsync(song: Song): Deferred<Result<Unit>> {
        logger.info("Updating public song: $song")
        song.versionNumber++
        song.updateTime = Date().time
        val dto = ChordsSongDto.fromModel(song)
        val json = jsonSerializer.encodeToString(ChordsSongDto.serializer(), dto)
        val request: Request = Request.Builder()
            .url(updatePublicSongIdUrl(song.id))
            .put(RequestBody.create(jsonType, json))
            .addHeader(authTokenHeader, adminService.userAuthToken)
            .build()
        return httpRequester.httpRequestAsync(request) { response: Response ->
            logger.debug("Update response", response.body()?.string())
        }
    }

    private fun approveAntechamberSongAsync(song: Song): Deferred<Result<Unit>> {
        logger.info("Approving antechamber song: $song")
        val dto = ChordsSongDto.fromModel(song)
        dto.id = null
        dto.categories = song.customCategoryName?.let { listOf(it) } ?: emptyList()
        val json = jsonSerializer.encodeToString(ChordsSongDto.serializer(), dto)
        val request: Request = Request.Builder()
            .url(approveSongUrl)
            .post(RequestBody.create(jsonType, json))
            .addHeader(authTokenHeader, adminService.userAuthToken)
            .build()
        return httpRequester.httpRequestAsync(request) { response: Response ->
            logger.debug("Approve response", response.body()?.string())
        }
    }

    fun updateAntechamberSongUI(song: Song) {
        uiInfoService.showInfo(R.string.admin_sending, indefinite = true)

        val deferred = updateAntechamberSongAsync(song)
        GlobalScope.launch(Dispatchers.Main) {
            val result = deferred.await()
            result.fold(onSuccess = {
                uiInfoService.showInfo(R.string.admin_success)
            }, onFailure = { e ->
                UiErrorHandler.handleError(e, R.string.admin_communication_breakdown)
            })
        }
    }

    fun approveCustomSongUI(song: Song) {
        val message1 =
            uiResourceService.resString(R.string.admin_antechamber_confirm_approve, song.toString())
        ConfirmDialogBuilder().confirmAction(message1) {
            uiInfoService.showInfo(R.string.admin_sending, indefinite = true)

            val deferred = approveAntechamberSongAsync(song)
            GlobalScope.launch(Dispatchers.Main) {
                val result = deferred.await()
                result.fold(onSuccess = {
                    uiInfoService.showInfo(R.string.admin_success)
                }, onFailure = { e ->
                    UiErrorHandler.handleError(e, R.string.admin_communication_breakdown)
                })
            }
        }
    }

    fun approveAntechamberSongUI(song: Song) {
        val msg = uiResourceService.resString(R.string.admin_antechamber_confirm_approve, song.toString())
        ConfirmDialogBuilder().confirmAction(msg) {
            uiInfoService.showInfo(R.string.admin_sending, indefinite = true)

            if (song.language == null) {
                song.language = SongLanguageDetector().detectLanguageCode(song.content.orEmpty())
            }

            GlobalScope.launch(Dispatchers.Main) {
                val result = approveAntechamberSongAsync(song).await()
                result.map { deleteAntechamberSongAsync(song).await() }
                result.fold(onSuccess = {
                    uiInfoService.showInfo(R.string.admin_success)
                    adminSongsLayoutContoller.fetchRequestSubject.onNext(true)
                }, onFailure = { e ->
                    UiErrorHandler.handleError(e, R.string.admin_communication_breakdown)
                })
            }
        }
    }

    fun deleteAntechamberSongUI(song: Song) {
        val message1 =
            uiResourceService.resString(R.string.admin_antechamber_confirm_delete, song.toString())
        ConfirmDialogBuilder().confirmAction(message1) {
            uiInfoService.showInfo(R.string.admin_sending, indefinite = true)

            GlobalScope.launch(Dispatchers.Main) {
                val result = deleteAntechamberSongAsync(song).await()
                result.fold(onSuccess = {
                    uiInfoService.showInfo(R.string.admin_success)
                    adminSongsLayoutContoller.fetchRequestSubject.onNext(true)
                }, onFailure = { e ->
                    UiErrorHandler.handleError(e, R.string.admin_communication_breakdown)
                })
            }
        }
    }

}
