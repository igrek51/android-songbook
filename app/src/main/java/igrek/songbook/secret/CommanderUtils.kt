package igrek.songbook.secret

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.ad.AdService
import igrek.songbook.persistence.LocalFilesystem
import igrek.songbook.persistence.repository.SongsRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File


@OptIn(DelicateCoroutinesApi::class)
class CommanderUtils(
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    adService: LazyInject<AdService> = appFactory.adService,
    localFilesystem: LazyInject<LocalFilesystem> = appFactory.localFilesystem,
) {
    private val activity by LazyExtractor(appCompatActivity)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val adService by LazyExtractor(adService)
    private val localDbService by LazyExtractor(localFilesystem)

    private val logger = LoggerFactory.logger

    fun backupDataFiles(cmd: String) {
        safeExecute {
            val parts = cmd.split(" ", limit = 2)
            check(parts.size == 2) { "insufficient sections" }
            val dataDirPath = localDbService.appFilesDir.absolutePath
            val localSrcFile = File(dataDirPath, parts[0])
            val localDstFile = File(parts[1])
            localSrcFile.copyTo(localDstFile)
            uiInfoService.showToast("File copied from $localSrcFile to $localDstFile")
        }
    }

    fun restoreDataFiles(cmd: String) {
        safeExecute {
            val parts = cmd.split(" ", limit = 2)
            check(parts.size == 2) { "insufficient sections" }
            val dataDirPath = localDbService.appFilesDir.absolutePath
            val localSrcFile = File(parts[0])
            val localDstFile = File(dataDirPath, parts[1])
            localSrcFile.copyTo(localDstFile, overwrite = true)
            uiInfoService.showToast("File copied from $localSrcFile to $localDstFile")
        }
    }

    fun shellCommand(cmd: String, showStdout: Boolean = false) {
        logger.debug("Running shell command: $cmd")
        safeExecute {
            val execute: Process = Runtime.getRuntime().exec(cmd)
            execute.waitFor()
            val retCode = execute.exitValue()

            val stdout = BufferedReader(execute.inputStream.reader()).use {
                val content = it.readText()
                content
            }
            val stderr = BufferedReader(execute.errorStream.reader()).use {
                val content = it.readText()
                content
            }
            logger.debug("command stdout: $stdout")
            logger.debug("command stderr: $stderr")

            when (showStdout) {
                true -> {
                    if (retCode == 0) {
                        uiInfoService.dialog(R.string.command_successful, "$stdout\n$stderr")
                    } else {
                        uiInfoService.dialog(
                            R.string.command_failed,
                            "$stdout\n$stderr\nerror code: $retCode"
                        )
                    }
                }
                false -> {
                    if (retCode == 0) {
                        uiInfoService.showToast("Command successful")
                    } else {
                        uiInfoService.showToast("Command failed ($retCode)")
                    }
                }
            }
        }
    }

    fun enableAds() {
        adService.enableAds()
    }

    fun disableAds() {
        adService.disableAds()
        uiInfoService.showToast(R.string.ads_disabled)
    }

    fun showCowSuperPowers() {
        GlobalScope.launch(Dispatchers.Main) {
            val alertBuilder = AlertDialog.Builder(activity)
            alertBuilder.setTitle("Moooo!")
            alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_info_ok)) { _, _ -> }
            alertBuilder.setCancelable(true)
            val dialog: AlertDialog = alertBuilder.create()

            val inflater =
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val itemView = inflater.inflate(R.layout.component_alert_monospace, null, false)
            val contentTextView = itemView.findViewById(R.id.contentTextView) as TextView
            contentTextView.text = EA5T3R_M00
            contentTextView.isVerticalScrollBarEnabled = true
            dialog.setView(itemView)

            if (!activity.isFinishing) {
                dialog.show()
            }

            uiInfoService.showToast(R.string.easter_egg_discovered)
        }
    }

    fun unlockSongs(key: String) {
        logger.info("unlocking songs with key $key")
        val toUnlock = songsRepository.publicSongsRepo.songs.get()
            .filter { s -> s.lockPassword == key }
        toUnlock.forEach { s ->
            s.locked = false
        }
        songsRepository.unlockedSongsDao.unlockKey(key)
        val unlocked = songsRepository.publicSongsRepo.songs.get()
            .count { s -> s.lockPassword == key }
        val message = uiResourceService.resString(R.string.unlock_new_songs_unlocked, unlocked)
        uiInfoService.showToast(message)
    }

    companion object {
        private const val EA5T3R_M00: String = """
     _____________________
    / Congratulations!    \
    |                     |
    | You have found a    |
    \ Secret Cow Level :) /
     ---------------------
       \   ^__^
        \  (oo)\_______
           (__)\       )\/\
               ||----w |
               ||     ||
    """
    }
}
