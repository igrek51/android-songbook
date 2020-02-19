package igrek.songbook.admin.antechamber

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dagger.Lazy
import igrek.songbook.activity.ActivityController
import igrek.songbook.admin.AdminService
import igrek.songbook.custom.CustomSongService
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import okhttp3.*
import java.io.IOException
import javax.inject.Inject


class AntechamberService {

    @Inject
    lateinit var activityController: Lazy<ActivityController>
    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var songContextMenuBuilder: SongContextMenuBuilder
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var songOpener: SongOpener
    @Inject
    lateinit var okHttpClient: Lazy<OkHttpClient>
    @Inject
    lateinit var adminService: Lazy<AdminService>
    @Inject
    lateinit var customSongService: CustomSongService

    companion object {
        private const val antechamberApiBase = "https://antechamber.chords.igrek.dev/api/v4"
        private const val chordsApiBase = "https://chords.igrek.dev/api/v5"

        private const val allSongsUrl = "$antechamberApiBase/songs"
        private val specificSongUrl = { id: Long -> "$antechamberApiBase/songs/$id" }
        private const val approveSongUrl = "$chordsApiBase/songs"

        private const val authTokenHeader = "X-Auth-Token"
    }

    private val jsonType = MediaType.parse("application/json; charset=utf-8")

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun downloadSongs(): Observable<List<Song>> {
        val request: Request = Request.Builder()
                .url(allSongsUrl)
                .addHeader(authTokenHeader, adminService.get().userAuthToken)
                .build()
        return httpRequest(request) { response: Response ->
            val json = response.body()?.string() ?: ""
            val mapper = jacksonObjectMapper()
            val allDtos: AllAntechamberSongsDto = mapper.readValue(json)
            val antechamberSongs: List<Song> = allDtos.toModel()
            antechamberSongs
        }
    }

    fun updateAntechamberSong(song: Song): Observable<Boolean> {
        val antechamberSongDto = AntechamberSongDto.fromModel(song)
        val mapper = jacksonObjectMapper()
        val json = mapper.writeValueAsString(antechamberSongDto)
        val request: Request = Request.Builder()
                .url(specificSongUrl(song.id))
                .put(RequestBody.create(jsonType, json))
                .addHeader(authTokenHeader, adminService.get().userAuthToken)
                .build()
        return httpRequest(request) { true }
    }

    fun createAntechamberSong(song: Song): Observable<Boolean> {
        logger.info("Sending new antechamber song")
        val antechamberSongDto = AntechamberSongDto.fromModel(song)
        antechamberSongDto.id = null
        val mapper = jacksonObjectMapper()
        val json = mapper.writeValueAsString(antechamberSongDto)
        val request: Request = Request.Builder()
                .url(allSongsUrl)
                .post(RequestBody.create(jsonType, json))
                .build()
        return httpRequest(request) { true }
    }

    fun deleteAntechamberSong(song: Song): Observable<Boolean> {
        val request: Request = Request.Builder()
                .url(specificSongUrl(song.id))
                .delete()
                .addHeader(authTokenHeader, adminService.get().userAuthToken)
                .build()
        return httpRequest(request) { true }
    }

    fun approveAntechamberSong(song: Song): Observable<Boolean> {
        logger.info("Approving antechamber song: $song")
        val dto = ChordsSongDto.fromModel(song)
        dto.id = null
        val mapper = jacksonObjectMapper()
        val json = mapper.writeValueAsString(dto)
        val request: Request = Request.Builder()
                .url(approveSongUrl)
                .post(RequestBody.create(jsonType, json))
                .addHeader(authTokenHeader, adminService.get().userAuthToken)
                .build()
        return httpRequest(request) { response: Response ->
            logger.debug("Approve response", response.body()?.string())
            true
        }
    }

    private fun <T> httpRequest(request: Request, successor: (Response) -> T): Observable<T> {
        val receiver = BehaviorSubject.create<T>()

        okHttpClient.get().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error("Request sending error: ${e.message}")
                receiver.onError(RuntimeException(e.message))
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    logger.error("Unexpected response code: $response")
                    receiver.onError(RuntimeException(response.toString()))
                } else {
                    val responseData = successor(response)
                    receiver.onNext(responseData)
                }
            }
        })

        return receiver
    }

}
