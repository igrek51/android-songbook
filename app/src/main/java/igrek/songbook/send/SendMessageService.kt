package igrek.songbook.send

import igrek.songbook.R
import igrek.songbook.admin.HttpRequester
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.system.PackageInfoService
import igrek.songbook.util.ioScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.Request

class SendMessageService(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    packageInfoService: LazyInject<PackageInfoService> = appFactory.packageInfoService,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val packageInfoService by LazyExtractor(packageInfoService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val layoutController by LazyExtractor(layoutController)

    private val httpRequester = HttpRequester()

    companion object {
        private const val url = "https://feedback.igrek.dev/api/v1/send"
        private const val APPLICATION_ID = 1
    }

    fun sendContactMessage(
        message: String, origin: MessageOrigin,
        author: String? = null,
        subject: String? = null,
        category: String? = null,
        title: String? = null,
        originalSongId: String? = null,
    ) {
        uiInfoService.showInfo(R.string.contact_sending, indefinite = true)
        ioScope.launch {
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
                .addFormDataPart("original_song_id", originalSongId ?: "")
                .addFormDataPart("application_id", APPLICATION_ID.toString())
                .addFormDataPart("app_version", "$appVersionName ($appVersionCode)")
                .addFormDataPart("db_version", dbVersionNumber)
                .build()

            val request = Request.Builder().url(url).post(requestBody).build()

            val result = httpRequester.httpRequestAsync(request) {
                it.body()?.string() ?: ""
            }.await()

            result.fold(onSuccess = { response: String ->
                if (response.startsWith("200")) {
                    delay(500) // if sending too fast, user is not sure if it has been sent
                    uiInfoService.showInfo(R.string.contact_message_sent_successfully)
                } else {
                    val error = RuntimeException("Bad response: $response")
                    UiErrorHandler.handleError(error, R.string.contact_error_sending)
                }
            }, onFailure = { e ->
                UiErrorHandler.handleError(e, R.string.contact_error_sending)
            })
        }
    }

    fun requestMissingSong() {
        layoutController.showLayout(MissingSongLayoutController::class)
    }
}
