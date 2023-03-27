package igrek.songbook.secret

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import igrek.songbook.layout.dialog.InputDialogBuilder
import igrek.songbook.persistence.LocalFilesystem
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.system.PermissionService
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
    permissionService: LazyInject<PermissionService> = appFactory.permissionService,
) {
    private val activity by LazyExtractor(appCompatActivity)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val adService by LazyExtractor(adService)
    private val localDbService by LazyExtractor(localFilesystem)
    private val permissionService by LazyExtractor(permissionService)

    private val logger = LoggerFactory.logger

    fun grantStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:" + activity.packageName)
            activity.startActivityForResult(intent, 0)
        }

        val granted = permissionService.ensureStorageWriteAccess()
        if (granted)
            success("Storage permission is granted")
    }

    fun backupAppDataLocalFile(cmd: String) {
        val parts = extractParameters(cmd)
        check(parts.size == 2) { "wrong number of arguments" }
        val dataDirPath = localDbService.appFilesDir.absolutePath
        val localSrcFile = File(dataDirPath, parts[0])
        val localDstFile = File(parts[1])
        localSrcFile.copyTo(localDstFile)
        success("File copied from $localSrcFile to $localDstFile")
    }

    fun restoreAppDataLocalFile(cmd: String) {
        val parts = extractParameters(cmd)
        check(parts.size == 2) { "wrong number of arguments" }
        val dataDirPath = localDbService.appFilesDir.absolutePath
        val localSrcFile = File(parts[0])
        val localDstFile = File(dataDirPath, parts[1])
        localSrcFile.copyTo(localDstFile, overwrite = true)
        success("File copied from $localSrcFile to $localDstFile")
    }

    fun backupAppDataDialog(cmd: String) {
        val parts = extractParameters(cmd)
        check(parts.size == 1) { "wrong number of arguments" }
        val dataDirPath = localDbService.appFilesDir.absolutePath
        val localSrcFile = File(dataDirPath, parts[0])
        val content = localSrcFile.readText(Charsets.UTF_8)
        dialogOutput(title = "File: ${localSrcFile.name}", message = content)
    }

    fun restoreAppDataDialog(cmd: String) {
        val parts = extractParameters(cmd)
        check(parts.size == 1) { "wrong number of arguments" }
        val dataDirPath = localDbService.appFilesDir.absolutePath
        val localDstFile = File(dataDirPath, parts[0])
        InputDialogBuilder().input("Enter file content", "", multiline = true) { content ->
            localDstFile.writeText(content, Charsets.UTF_8)
            success("File saved: $localDstFile")
        }
    }

    fun editAppDataDialog(cmd: String) {
        val parts = extractParameters(cmd)
        check(parts.size == 1) { "wrong number of arguments" }
        val dataDirPath = localDbService.appFilesDir.absolutePath
        val localFile = File(dataDirPath, parts[0])
        val originalContent = localFile.readText(Charsets.UTF_8)
        InputDialogBuilder().input("File content", originalContent, multiline = true) { content ->
            localFile.writeText(content, Charsets.UTF_8)
            success("File saved: $localFile")
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
                        dialogOutput(
                            titleResId = R.string.command_successful,
                            message = "$stdout\n$stderr"
                        )
                    } else {
                        dialogOutput(
                            titleResId = R.string.command_failed,
                            message = "$stdout\n$stderr\nerror code: $retCode"
                        )
                    }
                }
                false -> {
                    if (retCode == 0) {
                        success("Command successful")
                    } else {
                        success("Command failed ($retCode)")
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
        success(R.string.ads_disabled)
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
        success(message)
    }

    fun success(message: String) {
        uiInfoService.showSnackbar(
            info = message,
            actionResId = R.string.error_details,
            indefinite = false,
            action = {
                dialogOutput(message = message)
            },
        )
    }

    private fun success(messageResId: Int) {
        val message = uiInfoService.resString(messageResId)
        success(message)
    }

    private fun dialogOutput(
        titleResId: Int = 0, title: String = "",
        messageResId: Int = 0, message: String = "",
    ) {
        val messageIn = when {
            messageResId > 0 -> uiResourceService.resString(messageResId)
            else -> message
        }
        uiInfoService.dialogThreeChoices(
            titleResId = titleResId, title = title,
            message = messageIn,
            positiveButton = R.string.action_info_ok, positiveAction = {},
            neutralButton = R.string.action_copy_dialog,
            neutralAction = {
                appFactory.clipboardManager.get().copyToSystemClipboard(messageIn)
                uiInfoService.showToast(R.string.copied_to_clipboard)
            },
        )
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

            if (!activity.isFinishing)
                dialog.show()

            uiInfoService.showToast(R.string.easter_egg_discovered)
        }
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
