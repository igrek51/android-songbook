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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
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
) {
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val adminService by LazyExtractor(adminService)
    private val songsRepository by LazyExtractor(songsRepository)

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

    fun downloadSongs(): Observable<List<Song>> {
        val request: Request = Request.Builder()
                .url(allSongsUrl)
                .addHeader(authTokenHeader, adminService.userAuthToken)
                .build()
        return httpRequester.httpRequest(request) { response: Response ->
            val json = response.body()?.string() ?: ""
            val allDtos: AllAntechamberSongsDto = jsonSerializer.parse(AllAntechamberSongsDto.serializer(), json)
            val antechamberSongs: List<Song> = allDtos.toModel()
            antechamberSongs
        }
    }

    private fun updateAntechamberSong(song: Song): Observable<Boolean> {
        val antechamberSongDto = AntechamberSongDto.fromModel(song)
        val json = jsonSerializer.stringify(AntechamberSongDto.serializer(), antechamberSongDto)
        val request: Request = Request.Builder()
                .url(specificSongUrl(song.id))
                .put(RequestBody.create(jsonType, json))
                .addHeader(authTokenHeader, adminService.userAuthToken)
                .build()
        return httpRequester.httpRequest(request) { true }
    }

    fun createAntechamberSong(song: Song): Observable<Boolean> {
        logger.info("Sending new antechamber song")
        val antechamberSongDto = AntechamberSongDto.fromModel(song)
        antechamberSongDto.id = null
        val json = jsonSerializer.stringify(AntechamberSongDto.serializer(), antechamberSongDto)
        val request: Request = Request.Builder()
                .url(allSongsUrl)
                .post(RequestBody.create(jsonType, json))
                .build()
        return httpRequester.httpRequest(request) { true }
    }

    fun deleteAntechamberSong(song: Song): Observable<Boolean> {
        val request: Request = Request.Builder()
                .url(specificSongUrl(song.id))
                .delete()
                .addHeader(authTokenHeader, adminService.userAuthToken)
                .build()
        return httpRequester.httpRequest(request) { true }
    }

    fun updatePublicSong(song: Song): Observable<Boolean> {
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
        return httpRequester.httpRequest(request) { response: Response ->
            logger.debug("Update response", response.body()?.string())
            true
        }
    }

    private fun approveAntechamberSong(song: Song): Observable<Boolean> {
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
        return httpRequester.httpRequest(request) { response: Response ->
            logger.debug("Approve response", response.body()?.string())
            true
        }
    }

    private fun findCategoryByName(name: String): Category? {
        return songsRepository.allSongsRepo.publicCategories.get()
                .find { it.displayName == name }
    }

    fun updateAntechamberSongUI(song: Song) {
        uiInfoService.showInfoIndefinite(R.string.admin_sending)
        updateAntechamberSong(song)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    uiInfoService.showInfo(R.string.admin_success)
                }, { error ->
                    val message = uiResourceService.resString(R.string.admin_communication_breakdown, error.message)
                    uiInfoService.showInfoIndefinite(message)
                })
    }

    fun approveAntechamberSongUI(song: Song) {
        val message1 = uiResourceService.resString(R.string.admin_antechamber_confirm_approve, song.toString())
        ConfirmDialogBuilder().confirmAction(message1) {
            uiInfoService.showInfoIndefinite(R.string.admin_sending)
            approveAntechamberSong(song)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        uiInfoService.showInfo(R.string.admin_success)
                        deleteAntechamberSongUI(song)
                    }, { error ->
                        val message = uiResourceService.resString(R.string.admin_communication_breakdown, error.message)
                        uiInfoService.showInfoIndefinite(message)
                    })
        }
    }

    fun approveCustomSongUI(song: Song) {
        val message1 = uiResourceService.resString(R.string.admin_antechamber_confirm_approve, song.toString())
        ConfirmDialogBuilder().confirmAction(message1) {
            uiInfoService.showInfoIndefinite(R.string.admin_sending)
            approveAntechamberSong(song)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        uiInfoService.showInfo(R.string.admin_success)
                    }, { error ->
                        val message = uiResourceService.resString(R.string.admin_communication_breakdown, error.message)
                        uiInfoService.showInfoIndefinite(message)
                    })
        }
    }

    fun deleteAntechamberSongUI(song: Song) {
        val message1 = uiResourceService.resString(R.string.admin_antechamber_confirm_delete, song.toString())
        ConfirmDialogBuilder().confirmAction(message1) {
            uiInfoService.showInfoIndefinite(R.string.admin_sending)
            deleteAntechamberSong(song)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        uiInfoService.showInfo(R.string.admin_success)
                    }, { error ->
                        val message = uiResourceService.resString(R.string.admin_communication_breakdown, error.message)
                        uiInfoService.showInfoIndefinite(message)
                    })
        }
    }

}
