package igrek.songbook.secret

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.admin.AdminService
import igrek.songbook.billing.BillingLayoutController
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.analytics.CrashlyticsLogger
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.ad.AdService
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.system.PermissionService
import igrek.songbook.system.SoftKeyboardService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File


@OptIn(DelicateCoroutinesApi::class)
class SecretCommandService(
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
    preferencesService: LazyInject<PreferencesService> = appFactory.preferencesService,
    adminService: LazyInject<AdminService> = appFactory.adminService,
    adService: LazyInject<AdService> = appFactory.adService,
    permissionService: LazyInject<PermissionService> = appFactory.permissionService,
    localDbService: LazyInject<LocalDbService> = appFactory.localDbService,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
) {
    private val activity by LazyExtractor(appCompatActivity)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val preferencesService by LazyExtractor(preferencesService)
    private val adminService by LazyExtractor(adminService)
    private val adService by LazyExtractor(adService)
    private val permissionService by LazyExtractor(permissionService)
    private val localDbService by LazyExtractor(localDbService)
    private val activityController by LazyExtractor(activityController)
    private val layoutController by LazyExtractor(layoutController)

    private val logger = LoggerFactory.logger

    private val cmdRules: List<CommandRule> by lazy {
        listOf(
            CommandRule({
                it.matches("^m[ou]+$".toRegex())
            }) { showCowSuperPowers() },
            SimplifiedKeyRule("dupa", "okon") { showCowSuperPowers() },

            SimplifiedKeyRule("engineer", "inzynier") { unlockSongs("engineer") },

            ExactKeyRule("reset") {
                this.songsRepository.factoryReset()
                this.preferencesService.clear()
            },
            ExactKeyRule("reset config") { this.preferencesService.clear() },
            ExactKeyRule("reset db") { this.songsRepository.factoryReset() },
            ExactKeyRule("reset db general") {
                this.songsRepository.resetGeneralData()
                this.songsRepository.reloadSongsDb()
            },
            ExactKeyRule("reset db user") {
                this.songsRepository.resetUserData()
                this.songsRepository.reloadSongsDb()
            },
            ExactKeyRule("reload db user") {
                this.songsRepository.reloadSongsDb()
            },

            ExactKeyRule("firebase crashme") {
                logger.error(IllegalArgumentException("real reason"))
                throw RuntimeException("deliberate disaster")
            },
            ExactKeyRule("firebase error") {
                logger.error(IllegalArgumentException("real reason"))
                logger.error("error log")
                CrashlyticsLogger().sendCrashlytics()
            },

            ExactKeyRule("ad show") { this.adService.enableAds() },

            ExactKeyRule("grant permission files") {
                this.permissionService.isStoragePermissionGranted
            },

            CommandRule({ it.trim().matches(encodedSecretRegex) }) {
                decodeSecretKeys(it)
            },

            SubCommandRule("hush", ::hashedCommand),

            SubCommandRule("shell") { shellCommand(it, showStdout = false) },
            SubCommandRule("shellout") { shellCommand(it, showStdout = true) },

            SubCommandRule("unlock", ::unlockSongs),

            SubCommandRule("backup export local", ::backupDataFiles),
            SubCommandRule("backup import local", ::restoreDataFiles),

            SubCommandRule("login") { key: String ->
                this.adminService.loginAdmin(key)
            },

            ExactKeyRule("goto shop") {
                this.layoutController.showLayout(BillingLayoutController::class)
            },

            ExactKeyRule("exit now") {
                GlobalScope.launch(Dispatchers.Main) {
                    toast("exiting...")
                    this@SecretCommandService.activityController.quit()
                }
            },
        )
    }

    private val hashedCommands: Map<String, () -> Unit> by lazy {
        mapOf(
            "5581b03c159338d1e17cdc04c424788209a4c52cfa65c981af93de3a0600a427" to { disableAds() }
        )
    }

    private val encodedSecretRegex = Regex("""---SONGBOOK-KEY---([\S\s]+?)---SONGBOOK-KEY---""")

    private fun backupDataFiles(cmd: String) {
        SafeExecutor {
            val parts = cmd.split(" ", limit = 2)
            check(parts.size == 2) { "insufficient sections" }
            val dataDirPath = localDbService.appFilesDir.absolutePath
            val localSrcFile = File(dataDirPath, parts[0])
            val localDstFile = File(parts[1])
            localSrcFile.copyTo(localDstFile)
            toast("File copied from $localSrcFile to $localDstFile")
        }
    }

    private fun restoreDataFiles(cmd: String) {
        SafeExecutor {
            val parts = cmd.split(" ", limit = 2)
            check(parts.size == 2) { "insufficient sections" }
            val dataDirPath = localDbService.appFilesDir.absolutePath
            val localSrcFile = File(parts[0])
            val localDstFile = File(dataDirPath, parts[1])
            localSrcFile.copyTo(localDstFile, overwrite = true)
            toast("File copied from $localSrcFile to $localDstFile")
        }
    }

    private fun decodeSecretKeys(key: String) {
        val match = encodedSecretRegex.matchEntire(key.trim())
        match?.let {
            val encodedCommands = it.groupValues[1].trim().lines()
            encodedCommands.forEach { encCmd ->
                decodeSecretKey(encCmd.trim())
            }
        }
    }

    private fun decodeSecretKey(skey: String) {
        logger.debug("decoding secret key: $skey")
        SafeExecutor {
            val jwt = JWT(skey)
            when {
                "cmd" in jwt.claims -> {
                    jwt.claims["cmd"]?.asString()?.let { cmd ->
                        logger.debug("JWT decoded cmd: $cmd")
                        checkActivationRules(cmd)
                    }
                }
                "cmds" in jwt.claims -> {
                    jwt.claims["cmds"]?.asList(String::class.java)?.let { cmds ->
                        logger.debug("JWT decoded cmds: $cmds")
                        cmds.forEach { cmd ->
                            checkActivationRules(cmd)
                        }
                    }
                }
            }
        }
    }

    fun showUnlockAlert() {
        val secretTitle = uiResourceService.resString(R.string.action_secret)
        val dlgAlert = AlertDialog.Builder(activity)
        dlgAlert.setMessage(uiResourceService.resString(R.string.unlock_type_in_secret_key))
        dlgAlert.setTitle(secretTitle)

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        input.isSingleLine = false
        dlgAlert.setView(input)

        val actionCheck = uiResourceService.resString(R.string.action_check_secret)
        dlgAlert.setPositiveButton(actionCheck) { _, _ -> commandAttempt(input.text.toString()) }
        dlgAlert.setNegativeButton(uiResourceService.resString(R.string.action_cancel)) { _, _ -> }
        dlgAlert.setCancelable(true)
        dlgAlert.create().show()

        input.requestFocus()
        Handler(Looper.getMainLooper()).post {
            softKeyboardService.showSoftKeyboard(input)
        }
    }

    private fun commandAttempt(key: String) {
        logger.info("secret command entered: $key")

        GlobalScope.launch(Dispatchers.Main) {
            if (!checkActivationRules(key)) {
                toast(R.string.unlock_key_invalid)
            }
        }

        softKeyboardService.hideSoftKeyboard()
    }

    private fun checkActivationRules(key: String): Boolean {
        for (rule in cmdRules) {
            if (rule.condition(key)) {
                logger.debug("rule activated: $key")
                rule.activator(key)
                return true
            }
        }
        return false
    }

    private fun hashedCommand(cmd: String) {
        val hash = ShaHasher().hash(cmd)
        if (hash !in hashedCommands) {
            logger.warn("invalid hashed command: $cmd (#$hash)")
            toast("invalid \"hush\" command (#$hash)")
            return
        }
        logger.info("hashed command activated: $cmd (#$hash)")
        val command = hashedCommands[hash]
        command?.invoke()
    }

    private fun shellCommand(cmd: String, showStdout: Boolean = false) {
        logger.debug("Running shell command: $cmd")
        SafeExecutor {
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
                        uiInfoService.dialog(R.string.command_failed, "$stdout\n$stderr\nerror code: $retCode")
                    }
                }
                false -> {
                    if (retCode == 0) {
                        toast("Command successful")
                    } else {
                        toast("Command failed ($retCode)")
                    }
                }
            }
        }
    }

    private fun disableAds() {
        adService.disableAds()
        toast(R.string.ads_disabled)
    }

    private fun showCowSuperPowers() {
        GlobalScope.launch(Dispatchers.Main) {
            val alertBuilder = AlertDialog.Builder(activity)
            alertBuilder.setTitle("Moooo!")
            alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_info_ok)) { _, _ -> }
            alertBuilder.setCancelable(true)
            val dialog: AlertDialog = alertBuilder.create()

            val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val itemView = inflater.inflate(R.layout.component_alert_monospace, null, false)
            val contentTextView = itemView.findViewById(R.id.contentTextView) as TextView
            contentTextView.text = EA5T3R_M00
            contentTextView.isVerticalScrollBarEnabled = true
            dialog.setView(itemView)

            if (!activity.isFinishing) {
                dialog.show()
            }

            toast(R.string.easter_egg_discovered)
        }
    }

    private fun toast(message: String) {
        uiInfoService.showToast(message)
    }

    private fun toast(resId: Int) {
        uiInfoService.showToast(resId)
    }

    private fun unlockSongs(key: String) {
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
