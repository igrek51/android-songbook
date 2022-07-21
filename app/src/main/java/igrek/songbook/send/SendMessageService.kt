package igrek.songbook.send

import android.os.Handler
import android.os.Looper
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.system.PackageInfoService
import okhttp3.*
import java.io.IOException

class SendMessageService(
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
        okHttpClient: LazyInject<OkHttpClient> = appFactory.okHttpClient,
        packageInfoService: LazyInject<PackageInfoService> = appFactory.packageInfoService,
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
        layoutController: LazyInject<LayoutController> = appFactory.layoutController,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val okHttpClient by LazyExtractor(okHttpClient)
    private val packageInfoService by LazyExtractor(packageInfoService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val layoutController by LazyExtractor(layoutController)

    private val logger = LoggerFactory.logger

    companion object {
        private const val APPLICATION_ID = 1
        private const val url = "https://feedback.igrek.dev/api/v1/send"
    }

    fun sendContactMessage(message: String, origin: MessageOrigin,
                           author: String? = null,
                           subject: String? = null,
                           category: String? = null,
                           title: String? = null,
                           originalSongId: Long? = null
    ) {
        uiInfoService.showInfo(R.string.contact_sending, indefinite = true)

        val appVersionName = packageInfoService.versionName
        val appVersionCode = packageInfoService.versionCode.toString()
        val dbVersionNumber = songsRepository.publicSongsRepo.versionNumber.toString()

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("message", message)
                .addFormDataPart("subject", subject ?: "")
                .addFormDataPart("author", author ?: "")
                .addFormDataPart("title", title ?: "")
                .addFormDataPart("category", category ?: "")
                .addFormDataPart("origin_id", origin.id.toString())
                .addFormDataPart("original_song_id", originalSongId?.toString() ?: "")
                .addFormDataPart("application_id", APPLICATION_ID.toString())
                .addFormDataPart("app_version", "$appVersionName ($appVersionCode)")
                .addFormDataPart("db_version", dbVersionNumber)
                .build()

        val request = Request.Builder().url(url).post(requestBody).build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onErrorReceived(e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onErrorReceived("Unexpected code: $response")
                } else {
                    onResponseReceived(response.body()!!.string())
                }
            }
        })
    }

    private fun onResponseReceived(response: String) {
        logger.debug("Message sent response: $response")
        Handler(Looper.getMainLooper()).postDelayed({
            if (response.startsWith("200")) {
                uiInfoService.showInfo(R.string.contact_message_sent_successfully)
            } else {
                onErrorReceived("Contact message sent bad response: $response")
            }
        }, 500) // additional delay due to sending is "too fast" (user is not sure if it has been sent)
    }

    private fun onErrorReceived(errorMessage: String?) {
        logger.error("Contact message sending error: $errorMessage")
        Handler(Looper.getMainLooper()).post {
            uiInfoService.showInfo(R.string.contact_error_sending, indefinite = true)
        }
    }

    fun requestMissingSong() {
        layoutController.showLayout(MissingSongLayoutController::class)
    }

}
