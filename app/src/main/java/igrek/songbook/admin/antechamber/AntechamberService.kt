package igrek.songbook.admin.antechamber

import android.os.Handler
import android.os.Looper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dagger.Lazy
import igrek.songbook.R
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
        private const val apiUrl = "https://antechamber.chords.igrek.dev/api/v4"
        private const val allSongsUrl = "$apiUrl/songs"
        private val specificSongUrl = { id: Long -> "$apiUrl/songs/$id" }
        private const val authTokenHeader = "X-Auth-Token"
    }

    private val jsonType = MediaType.parse("application/json; charset=utf-8")

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun downloadSongs(): Observable<List<Song>> {
        val receiver = BehaviorSubject.create<List<Song>>()

        val request: Request = Request.Builder()
                .url(allSongsUrl)
                .addHeader(authTokenHeader, adminService.get().userAuthToken)
                .build()

        okHttpClient.get().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onErrorReceived(e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onErrorReceived("Unexpected code: $response")
                } else {
                    val json = response.body()?.string() ?: ""
                    val mapper = jacksonObjectMapper()
                    val allDtos: AllAntechamberSongsDto = mapper.readValue(json)
                    val antechamberSongs: List<Song> = allDtos.toModel()
                    receiver.onNext(antechamberSongs)
                }
            }
        })

        return receiver
    }

    private fun onErrorReceived(errorMessage: String?) {
        logger.error("Contact message sending error: $errorMessage")
        Handler(Looper.getMainLooper()).post {
            val message = uiResourceService.resString(R.string.admin_communication_breakdown, errorMessage)
            uiInfoService.showInfoIndefinite(message)
        }
    }

    fun updateAntechamberSong(song: Song) {
        val antechamberSongDto = AntechamberSongDto.fromModel(song)
        val mapper = jacksonObjectMapper()
        val json = mapper.writeValueAsString(antechamberSongDto)
        val request: Request = Request.Builder()
                .url(specificSongUrl(song.id))
                .put(RequestBody.create(jsonType, json))
                .addHeader(authTokenHeader, adminService.get().userAuthToken)
                .build()

        okHttpClient.get().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onErrorReceived(e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onErrorReceived("Unexpected code: $response")
                } else {
                    Handler(Looper.getMainLooper()).post {
                        uiInfoService.showInfo(R.string.admin_success)
                    }
                }
            }
        })
    }

    fun createAntechamberSong(song: Song) {
        logger.info("Sending new antechamber song")
        val antechamberSongDto = AntechamberSongDto.fromModel(song)
        antechamberSongDto.id = null
        val mapper = jacksonObjectMapper()
        val json = mapper.writeValueAsString(antechamberSongDto)
        val request: Request = Request.Builder()
                .url(allSongsUrl)
                .post(RequestBody.create(jsonType, json))
                .build()

        okHttpClient.get().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onErrorReceived(e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onErrorReceived("Unexpected code: $response")
                } else {
                    Handler(Looper.getMainLooper()).post {
                        uiInfoService.showInfo(R.string.antechamber_new_song_sent)
                    }
                }
            }
        })
    }

    fun deleteAntechamberSong(song: Song): Observable<Boolean> {
        val receiver = BehaviorSubject.create<Boolean>()

        val request: Request = Request.Builder()
                .url(specificSongUrl(song.id))
                .delete()
                .addHeader(authTokenHeader, adminService.get().userAuthToken)
                .build()

        okHttpClient.get().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onErrorReceived(e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onErrorReceived("Unexpected code: $response")
                } else {
                    receiver.onNext(true)
                }
            }
        })

        return receiver
    }

}
