package igrek.songbook.admin.antechamber

import dagger.Lazy
import igrek.songbook.admin.AdminService
import igrek.songbook.admin.HttpRequester
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import io.reactivex.Observable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import javax.inject.Inject


class SongRankService {

    @Inject
    lateinit var songsRepository: SongsRepository

    @Inject
    lateinit var uiResourceService: UiResourceService

    @Inject
    lateinit var uiInfoService: UiInfoService

    @Inject
    lateinit var adminService: Lazy<AdminService>

    companion object {
        private const val chordsApiBase = "https://chords.igrek.dev/api/v5"

        private val updatePublicSongIdUrl = { id: Long -> "${chordsApiBase}/songs/$id" }

        private const val authTokenHeader = "X-Auth-Token"
    }

    private val httpRequester = HttpRequester()
    private val jsonType = MediaType.parse("application/json; charset=utf-8")
    private val jsonSerializer = Json(JsonConfiguration.Stable)

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun updateRank(song: Song, rank: Double?): Observable<Boolean> {
        song.rank = rank
        logger.info("Updating song rank: $song")
        val dto = SongRankUpdateDto(rank = song.rank)
        val json = jsonSerializer.stringify(SongRankUpdateDto.serializer(), dto)
        val request: Request = Request.Builder()
                .url(updatePublicSongIdUrl(song.id))
                .put(RequestBody.create(jsonType, json))
                .addHeader(authTokenHeader, adminService.get().userAuthToken)
                .build()
        return httpRequester.httpRequest(request) { response: Response ->
            logger.debug("Update rank response", response.body()?.string())
            true
        }
    }

}

@Serializable
data class SongRankUpdateDto(
        var rank: Double? = null,
)
