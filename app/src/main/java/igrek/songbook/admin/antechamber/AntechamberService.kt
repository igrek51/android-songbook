package igrek.songbook.admin.antechamber


import igrek.songbook.R
import igrek.songbook.admin.AdminService
import igrek.songbook.admin.HttpRequester
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.util.*

class AntechamberService(
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        adminService: LazyInject<AdminService> = appFactory.adminService,
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
        adminSongsLayoutContoller: LazyInject<AdminSongsLayoutContoller> = appFactory.adminSongsLayoutContoller,
) {
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val adminService by LazyExtractor(adminService)
    private val songsRepository by LazyExtractor(songsRepository)
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
    private val jsonSerializer = Json(JsonConfiguration.Stable)

    fun downloadSongs(): Deferred<Result<List<Song>>> {
        val request: Request = Request.Builder()
                .url(allSongsUrl)
                .addHeader(authTokenHeader, adminService.userAuthToken)
                .build()
        return httpRequester.httpRequestAsync(request) { response: Response ->
            val json = response.body()?.string() ?: ""
            val allDtos: AllAntechamberSongsDto = jsonSerializer.parse(AllAntechamberSongsDto.serializer(), json)
            val antechamberSongs: List<Song> = allDtos.toModel()
            antechamberSongs
        }
    }

    private fun updateAntechamberSong(song: Song): Deferred<Result<Unit>> {
        val antechamberSongDto = AntechamberSongDto.fromModel(song)
        val json = jsonSerializer.stringify(AntechamberSongDto.serializer(), antechamberSongDto)
        val request: Request = Request.Builder()
                .url(specificSongUrl(song.id))
                .put(RequestBody.create(jsonType, json))
                .addHeader(authTokenHeader, adminService.userAuthToken)
                .build()
        return httpRequester.httpRequestAsync(request) { }
    }

    fun createAntechamberSong(song: Song): Deferred<Result<Unit>> {
        logger.info("Sending new antechamber song")
        val antechamberSongDto = AntechamberSongDto.fromModel(song)
        antechamberSongDto.id = null
        val json = jsonSerializer.stringify(AntechamberSongDto.serializer(), antechamberSongDto)
        val request: Request = Request.Builder()
                .url(allSongsUrl)
                .post(RequestBody.create(jsonType, json))
                .build()
        return httpRequester.httpRequestAsync(request) { }
    }

    fun deleteAntechamberSong(song: Song): Deferred<Result<Unit>> {
        val request: Request = Request.Builder()
                .url(specificSongUrl(song.id))
                .delete()
                .addHeader(authTokenHeader, adminService.userAuthToken)
                .build()
        return httpRequester.httpRequestAsync(request) { }
    }

    fun updatePublicSong(song: Song): Deferred<Result<Unit>> {
        logger.info("Updating public song: $song")
        song.versionNumber++
        song.updateTime = Date().time
        val dto = ChordsSongDto.fromModel(song)
        val json = jsonSerializer.stringify(ChordsSongDto.serializer(), dto)
        val request: Request = Request.Builder()
                .url(updatePublicSongIdUrl(song.id))
                .put(RequestBody.create(jsonType, json))
                .addHeader(authTokenHeader, adminService.userAuthToken)
                .build()
        return httpRequester.httpRequestAsync(request) { response: Response ->
            logger.debug("Update response", response.body()?.string())
        }
    }

    private fun approveAntechamberSong(song: Song): Deferred<Result<Unit>> {
        logger.info("Approving antechamber song: $song")
        val dto = ChordsSongDto.fromModel(song)
        dto.id = null
        val categoryId = song.customCategoryName?.let { findCategoryByName(it) }?.id
        dto.categories = categoryId?.let { listOf(it) } ?: emptyList()
        val json = jsonSerializer.stringify(ChordsSongDto.serializer(), dto)
        val request: Request = Request.Builder()
                .url(approveSongUrl)
                .post(RequestBody.create(jsonType, json))
                .addHeader(authTokenHeader, adminService.userAuthToken)
                .build()
        return httpRequester.httpRequestAsync(request) { response: Response ->
            logger.debug("Approve response", response.body()?.string())
        }
    }

    private fun findCategoryByName(name: String): Category? {
        return songsRepository.allSongsRepo.publicCategories.get()
                .find { it.displayName == name }
    }

    fun updateAntechamberSongUI(song: Song) {
        uiInfoService.showInfoIndefinite(R.string.admin_sending)

        val deferred = updateAntechamberSong(song)
        GlobalScope.launch(Dispatchers.Main) {
            val result = deferred.await()
            result.fold(onSuccess = {
                uiInfoService.showInfo(R.string.admin_success)
            }, onFailure = { e ->
                val message = uiResourceService.resString(R.string.admin_communication_breakdown, e.message)
                uiInfoService.showInfoIndefinite(message)
            })
        }
    }

    fun approveCustomSongUI(song: Song) {
        val message1 = uiResourceService.resString(R.string.admin_antechamber_confirm_approve, song.toString())
        ConfirmDialogBuilder().confirmAction(message1) {
            uiInfoService.showInfoIndefinite(R.string.admin_sending)

            val deferred = approveAntechamberSong(song)
            GlobalScope.launch(Dispatchers.Main) {
                val result = deferred.await()
                result.fold(onSuccess = {
                    uiInfoService.showInfo(R.string.admin_success)
                }, onFailure = { e ->
                    val message = uiResourceService.resString(R.string.admin_communication_breakdown, e.message)
                    uiInfoService.showInfoIndefinite(message)
                })
            }
        }
    }

    fun approveAntechamberSongUI(song: Song) {
        val message1 = uiResourceService.resString(R.string.admin_antechamber_confirm_approve, song.toString())
        ConfirmDialogBuilder().confirmAction(message1) {
            uiInfoService.showInfoIndefinite(R.string.admin_sending)

            GlobalScope.launch(Dispatchers.Main) {
                val result = approveAntechamberSong(song).await()
                result.map { deleteAntechamberSong(song).await() }
                result.fold(onSuccess = {
                    uiInfoService.showInfo(R.string.admin_success)
                    adminSongsLayoutContoller.fetchRequestSubject.onNext(true)
                }, onFailure = { e ->
                    val message = uiResourceService.resString(R.string.admin_communication_breakdown, e.message)
                    uiInfoService.showInfoIndefinite(message)
                })
            }
        }
    }

    fun deleteAntechamberSongUI(song: Song) {
        val message1 = uiResourceService.resString(R.string.admin_antechamber_confirm_delete, song.toString())
        ConfirmDialogBuilder().confirmAction(message1) {
            uiInfoService.showInfoIndefinite(R.string.admin_sending)

            GlobalScope.launch(Dispatchers.Main) {
                val result = deleteAntechamberSong(song).await()
                result.fold(onSuccess = {
                    uiInfoService.showInfo(R.string.admin_success)
                    adminSongsLayoutContoller.fetchRequestSubject.onNext(true)
                }, onFailure = { e ->
                    val message = uiResourceService.resString(R.string.admin_communication_breakdown, e.message)
                    uiInfoService.showInfoIndefinite(message)
                })
            }
        }
    }

}
