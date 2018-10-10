package igrek.songbook.persistence

import android.os.Handler
import android.os.Looper
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.logger.LoggerFactory
import okhttp3.*
import java.io.*
import javax.inject.Inject


class SongsUpdater {

    @Inject
    lateinit var okHttpClient: OkHttpClient
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var songsRepository: Lazy<SongsRepository>

    private val logger = LoggerFactory.getLogger()

    private val songsdbUrl = "http://51.38.128.10:8007/api/v1/songs"

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun updateSongsDb(songsDbFile: File) {

        uiInfoService.showInfoIndefinite(R.string.updating_db_in_progress)

        val request: Request = Request.Builder()
                .url(songsdbUrl)
                .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onErrorReceived(e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onErrorReceived("Unexpected code: $response")
                } else {
                    onResponseReceived(response, songsDbFile)
                }
            }
        })
    }

    private fun onResponseReceived(response: Response, songsDbFile: File) {
        try {
            val inputStream: InputStream = response.body()!!.byteStream()

            val input = BufferedInputStream(inputStream)
            val output = FileOutputStream(songsDbFile)

            val data = ByteArray(1024)

            var total: Long = 0
            var count: Int

            while (true) {
                count = input.read(data)
                if (count == -1)
                    break
                total += count
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()

            Handler(Looper.getMainLooper()).post {
                songsRepository.get().initializeSongsDb()
                uiInfoService.showInfo(R.string.ui_db_is_uptodate)
            }

        } catch (e: Exception) {
            onErrorReceived(e.message)
        }
    }

    private fun onErrorReceived(errorMessage: String?) {
        logger.error("Connection error: $errorMessage")
        Handler(Looper.getMainLooper()).post {
            uiInfoService.showInfoIndefinite(R.string.connection_error)
        }
    }

}