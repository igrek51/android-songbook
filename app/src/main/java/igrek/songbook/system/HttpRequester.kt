package igrek.songbook.system

import igrek.songbook.info.errorcheck.ContextError
import igrek.songbook.info.logger.LoggerFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class HttpRequester {

    private val okHttpClient: OkHttpClient = OkHttpClient()
    private val logger = LoggerFactory.logger

    val jsonType = MediaType.parse("application/json; charset=utf-8")
    val jsonSerializer = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = false
        allowStructuredMapKeys = true
        prettyPrint = false
        useArrayPolymorphism = false
    }

    fun <T> httpRequestAsync(
        request: Request,
        responseExtractor: suspend (Response) -> T,
    ): Deferred<Result<T>> {
        return GlobalScope.async {
            httpRequestSync(request, responseExtractor)
        }
    }

    private suspend fun <T> httpRequestSync(
        request: Request,
        responseExtractor: suspend (Response) -> T,
    ): Result<T> {
        try {

            val response: Response = okHttpClient.newCall(request).execute()
            return if (!response.isSuccessful) {
                val errorMessage = extractErrorMessage(response)
                Result.failure(ApiResponseError(errorMessage, response))
            } else {
                try {
                    val responseData = responseExtractor(response)
                    Result.success(responseData)
                } catch (e: Throwable) {
                    logger.error("onResponse error: ${e.message}", e)
                    Result.failure(ContextError("Network error: failed to extract response from ${request.method()} ${request.url()}", e))
                }
            }

        } catch (e: IOException) {
            logger.error("Request sending error: ${e.message}", e)
            return Result.failure(ContextError("Network error when sending request ${request.method()} ${request.url()}", e))
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
                logger.warn("Error deserializing error response: $jsonData", e)
            } catch (e: IllegalArgumentException) {
                logger.warn("Error deserializing error response: $jsonData", e)
            }
        }
        return "Unexpected Server response: ${response.message()}, code: ${response.code()}, url: ${response.request().method()} ${response.request().url()}"
    }

}

class ApiResponseError(
    message: String,
    val response: Response,
) : RuntimeException(message)

@Serializable
data class ErrorDto(
    var error: String,
    var type: String?,
)