package igrek.songbook.admin

import igrek.songbook.info.logger.LoggerFactory
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import okhttp3.*
import java.io.IOException

class HttpRequester {

    private val okHttpClient: OkHttpClient = OkHttpClient()
    private val logger = LoggerFactory.logger

    fun <T> httpRequest(request: Request, successor: (Response) -> T): Observable<T> {
        val receiver = BehaviorSubject.create<T>()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error("Request sending error: ${e.message}", e)
                receiver.onError(RuntimeException(e.message))
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    logger.error("Unexpected response code: $response")
                    receiver.onError(RuntimeException(response.toString()))
                } else {
                    try {
                        val responseData = successor(response)
                        receiver.onNext(responseData)
                    } catch (e: Throwable) {
                        logger.error("onResponse error: ${e.message}", e)
                        receiver.onError(RuntimeException(e.message))
                    }
                }
            }
        })

        return receiver
    }

}