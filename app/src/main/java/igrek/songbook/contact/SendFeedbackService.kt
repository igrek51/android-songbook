package igrek.songbook.contact

import android.app.Activity
import android.os.Handler
import android.os.Looper
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.system.PackageInfoService
import okhttp3.*
import java.io.IOException
import javax.inject.Inject

class SendFeedbackService {

    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var okHttpClient: OkHttpClient
    @Inject
    lateinit var packageInfoService: PackageInfoService
    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var layoutController: dagger.Lazy<LayoutController>
    @Inject
    lateinit var contactLayoutController: dagger.Lazy<ContactLayoutController>

    private val logger = LoggerFactory.logger

    companion object {
        private const val APPLICATION_ID = 1
        private const val url = "http://51.38.128.10:8006/contact/send/"
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun sendFeedback(message: String, author: String, subject: String) {
        uiInfoService.showInfo(uiResourceService.resString(R.string.contact_sending))

        val appVersionName = packageInfoService.versionName
        val appVersionCode = packageInfoService.versionCode.toString()
        val dbVersionNumber = songsRepository.songsDb!!.versionNumber.toString()

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("message", message)
                .addFormDataPart("author", author).addFormDataPart("subject", subject)
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
        logger.debug("Feedback sent response: $response")
        Handler(Looper.getMainLooper()).postDelayed({
            if (response.startsWith("200")) {
                uiInfoService.showInfo(R.string.contact_message_sent_successfully)
            } else {
                onErrorReceived("Feedback sent bad response: $response")
            }
        }, 500) // additional delay due to sending is "too fast" (user is not sure if it has been sent)
    }

    private fun onErrorReceived(errorMessage: String?) {
        logger.error("Feedback sending error: $errorMessage")
        Handler(Looper.getMainLooper()).post { uiInfoService.showInfoIndefinite(R.string.contact_error_sending) }
    }

    fun amendSong(song: Song) {
        layoutController.get().showContact()
        contactLayoutController.get().prepareSongAmend(song)
    }

    fun publishSong(song: Song) {
        layoutController.get().showContact()
        contactLayoutController.get().prepareCustomSongPublishing(song.title, song.customCategoryName, song.content)
    }

}
