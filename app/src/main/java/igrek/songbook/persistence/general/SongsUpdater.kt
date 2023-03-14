package igrek.songbook.persistence.general


import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.preferences.PreferencesState
import kotlinx.coroutines.*
import okhttp3.*
import java.io.*

@OptIn(DelicateCoroutinesApi::class)
class SongsUpdater(
    okHttpClient: LazyInject<OkHttpClient> = appFactory.okHttpClient,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    localDbService: LazyInject<LocalDbService> = appFactory.localDbService,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
) {
    private val okHttpClient by LazyExtractor(okHttpClient)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val localDbService by LazyExtractor(localDbService)
    private val preferencesState by LazyExtractor(preferencesState)

    private val logger = LoggerFactory.logger

    companion object {
        private const val apiUrl = "https://chords.igrek.dev/api/v5"
        private const val songsdbUrl = "$apiUrl/songs_db"
        private const val songsDbVersionUrl = "$apiUrl/songs_version"
    }

    fun updateSongsDbAsync(forced: Boolean) {
        GlobalScope.launch(Dispatchers.IO) {
            safeExecute {
                updateSongsDb(forced)
            }
        }
    }

    private fun updateSongsDb(forced: Boolean) {
        val songsDbFile: File = localDbService.songsDbFile

        uiInfoService.showInfo(R.string.updating_db_in_progress, indefinite = true)

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
                    GlobalScope.launch(Dispatchers.IO) {
                        onSongDatabaseReceived(response, songsDbFile, forced)
                    }
                }
            }
        })
    }

    fun checkUpdateIsAvailable() {
        GlobalScope.launch(Dispatchers.IO) {
            val request: Request = Request.Builder()
                .url(songsDbVersionUrl)
                .build()

            okHttpClient.newCall(request).enqueue(object : Callback {
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

    private suspend fun onSongDatabaseReceived(
        response: Response,
        songsDbFile: File,
        forcedUpdate: Boolean
    ) {
        try {
            val inputStream: InputStream = response.body()!!.byteStream()
            val input = BufferedInputStream(inputStream)
            try {
                songsRepository.close()
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
            } catch (e: Throwable) {
                songsRepository.saveAndReloadAllSongs()
                throw e
            }

            GlobalScope.launch (Dispatchers.IO) {
                try {
                    songsRepository.saveAndReloadAllSongs()
                    if (forcedUpdate) {
                        uiInfoService.showInfo(R.string.ui_db_is_uptodate)
                    } else {
                        uiInfoService.showInfo(R.string.ui_db_has_been_updated)
                    }
                } catch (t: Throwable) {
                    logger.error("Reloading songs db failed: ${t.message}")
                    uiInfoService.showInfo(R.string.db_update_failed_incompatible)
                    songsRepository.resetGeneralSongsData()
                    songsRepository.saveAndReloadAllSongs()
                }
            }
        } catch (e: Throwable) {
            logger.error("Downloading new database failed: ${e.message}")
            UiErrorHandler().handleError(
                RuntimeException("Downloading new database failed: ${e.message}"),
                R.string.error_communication_breakdown
            )
        }
    }

    private fun onSongDatabaseVersionReceived(response: Response) {
        try {
            val remoteVersion = response.body()?.string()?.toLong()
            val localVersion = songsRepository.songsDbVersion()

            logger.debug("DB Update availability check: local: $localVersion, remote: $remoteVersion")

            if (localVersion != null && remoteVersion != null && localVersion < remoteVersion) {
                if (preferencesState.updateDbOnStartup) {
                    updateSongsDbAsync(forced = false)
                } else {
                    showUpdateIsAvailable()
                }
            }
        } catch (e: Throwable) {
            logger.error(e)
        }
    }

    private fun onErrorReceived(errorMessage: String?) {
        logger.error("Connection error: $errorMessage")
        UiErrorHandler().handleError(
            RuntimeException("Connection error: $errorMessage"),
            R.string.error_communication_breakdown
        )
    }

    private fun showUpdateIsAvailable() {
        uiInfoService.showInfoAction(
            R.string.update_is_available,
            indefinite = true,
            actionResId = R.string.action_update
        ) {
            uiInfoService.clearSnackBars()
            updateSongsDbAsync(forced = false)
        }
    }

}