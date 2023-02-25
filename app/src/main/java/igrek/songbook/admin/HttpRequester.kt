package igrek.songbook.admin

import igrek.songbook.info.logger.LoggerFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

@OptIn(DelicateCoroutinesApi::class)
class HttpRequester {

    private val okHttpClient: OkHttpClient = OkHttpClient()
    private val logger = LoggerFactory.logger

    val jsonType = MediaType.parse("application/json; charset=utf-8")
    val jsonSerializer = Json {
        encodeDefaults = true
        ignoreUnknownKeys = false
        isLenient = false
        allowStructuredMapKeys = true
        prettyPrint = false
        useArrayPolymorphism = false
    }

    fun <T> httpRequestAsync(request: Request, responseExtractor: (Response) -> T): Deferred<Result<T>> {
        return GlobalScope.async {
            httpRequestSync(request, responseExtractor)
        }
    }

    private fun <T> httpRequestSync(request: Request, responseExtractor: (Response) -> T): Result<T> {
        try {

            val response: Response = okHttpClient.newCall(request).execute()
            return if (!response.isSuccessful) {
                val errorMessage = extractErrorMessage(response)
                Result.failure(RuntimeException(errorMessage))
            } else {
                try {
                    val responseData = responseExtractor(response)
                    Result.success(responseData)
                } catch (e: Throwable) {
                    logger.error("onResponse error: ${e.message}", e)
                    Result.failure(RuntimeException(e.message))
                }
            }

        } catch (e: IOException) {
            logger.error("Request sending error: ${e.message}", e)
            return Result.failure(RuntimeException(e.message))
        }
    }

    private fun extractErrorMessage(response: Response): String {
        val contentType = response.body()?.contentType()?.toString().orEmpty()
        if ("application/json" in contentType) {
            val jsonData = response.body()?.string() ?: ""
            try {
                val errorDto: ErrorDto = jsonSerializer.decodeFromString(ErrorDto.serializer(), jsonData)
                val errorDetails = errorDto.error
                return "Server response: $errorDetails, code: ${response.code()}, url: ${response.request().method()} ${response.request().url()}"
            } catch (e: kotlinx.serialization.SerializationException) {
            } catch (e: IllegalArgumentException) {
            }
        }
        return "Unexpected Server response: ${response.message()}, code: ${response.code()}, url: ${response.request().method()} ${response.request().url()}"
    }

}

@Serializable
data class ErrorDto(
    var error: String,
    var type: String?,
)