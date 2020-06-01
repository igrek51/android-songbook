package igrek.songbook.admin


import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import kotlinx.coroutines.Deferred
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

class AdminCategoryManager(
        adminService: LazyInject<AdminService> = appFactory.adminService,
) {
    private val adminService by LazyExtractor(adminService)

    companion object {
        private const val chordsApiBase = "https://chords.igrek.dev/api/v5"

        private const val createCategoryUrl = "$chordsApiBase/category"

        private const val authTokenHeader = "X-Auth-Token"
    }

    private val httpRequester = HttpRequester()
    private val jsonType = MediaType.parse("application/json; charset=utf-8")
    private val jsonSerializer = Json(JsonConfiguration.Stable)

    fun createCategory(categoryName: String): Deferred<Result<Unit>> {
        logger.info("Creating category: $categoryName")
        val dto = CreateCategoryDto(name = categoryName)
        val json = jsonSerializer.stringify(CreateCategoryDto.serializer(), dto)
        val request: Request = Request.Builder()
                .url(createCategoryUrl)
                .post(RequestBody.create(jsonType, json))
                .addHeader(authTokenHeader, adminService.userAuthToken)
                .build()
        return httpRequester.httpRequestAsync(request) { response: Response ->
            logger.debug("Add category response", response.body()?.string())
        }
    }
}

@Serializable
data class CreateCategoryDto(
        var name: String? = null,
)
