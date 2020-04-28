package igrek.songbook.admin.antechamber


import igrek.songbook.admin.AdminService
import igrek.songbook.admin.HttpRequester
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.general.model.Song
import io.reactivex.Observable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

class SongRankService(
        adminService: LazyInject<AdminService> = appFactory.adminService,
) {
    private val adminService by LazyExtractor(adminService)

    companion object {
        private const val chordsApiBase = "https://chords.igrek.dev/api/v5"

        private val updatePublicSongIdUrl = { id: Long -> "${chordsApiBase}/songs/$id" }

        private const val authTokenHeader = "X-Auth-Token"
    }

    private val httpRequester = HttpRequester()
    private val jsonType = MediaType.parse("application/json; charset=utf-8")
    private val jsonSerializer = Json(JsonConfiguration.Stable)

    fun updateRank(song: Song, rank: Double?): Observable<Boolean> {
        song.rank = rank
        logger.info("Updating song rank: $song")
        val dto = SongRankUpdateDto(rank = song.rank)
        val json = jsonSerializer.stringify(SongRankUpdateDto.serializer(), dto)
        val request: Request = Request.Builder()
                .url(updatePublicSongIdUrl(song.id))
                .put(RequestBody.create(jsonType, json))
                .addHeader(authTokenHeader, adminService.userAuthToken)
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
