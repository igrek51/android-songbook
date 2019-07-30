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
    lateinit var okHttpClient: Lazy<OkHttpClient>
    @Inject
    lateinit var uiInfoService: Lazy<UiInfoService>
    @Inject
    lateinit var songsRepository: Lazy<SongsRepository>
    @Inject
    lateinit var localDbService: Lazy<LocalDbService>

    private val logger = LoggerFactory.logger

    companion object {
        private const val apiUrl = "http://51.38.128.10:8008/api/v3/"
        private const val songsdbUrl = apiUrl + "songs"
        private const val songsDbVersionUrl = apiUrl + "songs_version"
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun updateSongsDb(songsDbFile: File) {

        uiInfoService.get().showInfoIndefinite(R.string.updating_db_in_progress)

        val request: Request = Request.Builder()
                .url(songsdbUrl)
                .build()

        okHttpClient.get().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onErrorReceived(e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onErrorReceived("Unexpected code: $response")
                } else {
                    onSongDatabaseReceived(response, songsDbFile)
                }
            }
        })
    }

    fun checkUpdateIsAvailable() {
        Handler(Looper.getMainLooper()).post {

            val request: Request = Request.Builder()
                    .url(songsDbVersionUrl)
                    .build()

            okHttpClient.get().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logger.error(e.message)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        logger.error("Unexpected code: $response")
                    } else {
                        onSongDatabaseVersionReceived(response)
                    }
                }
            })

        }
    }

    private fun onSongDatabaseReceived(response: Response, songsDbFile: File) {
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
                try {
                    songsRepository.get().initializeSongsDb()
                    uiInfoService.get().showInfo(R.string.ui_db_is_uptodate)
                } catch (t: Throwable) {
                    uiInfoService.get().showInfo(R.string.db_update_failed_incompatible)
                    localDbService.get().factoryResetSongsDb()
                }
            }
        } catch (e: Exception) {
            onErrorReceived(e.message)
        }
    }

    private fun onSongDatabaseVersionReceived(response: Response) {
        try {
            val remoteVersion = response.body()?.string()?.toLong()
            val localVersion = songsRepository.get().getSongsDbVersion()

            logger.debug("Update availability check: local: $localVersion, remote: $remoteVersion")

            if (localVersion != null && remoteVersion != null && localVersion < remoteVersion) {
                showUpdateIsAvailable()
            }
        } catch (e: Exception) {
            logger.error(e)
        }
    }

    private fun onErrorReceived(errorMessage: String?) {
        logger.error("Connection error: $errorMessage")
        Handler(Looper.getMainLooper()).post {
            uiInfoService.get().showInfoIndefinite(R.string.connection_error)
        }
    }

    private fun showUpdateIsAvailable() {
        Handler(Looper.getMainLooper()).post {
            uiInfoService.get().showInfoWithAction(R.string.update_is_available, R.string.action_update) {
                uiInfoService.get().clearSnackBars()
                songsRepository.get().updateSongsDb()
            }
        }
    }

}