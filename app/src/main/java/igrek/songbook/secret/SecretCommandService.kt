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
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.analytics.CrashlyticsLogger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.ad.AdService
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.system.PermissionService
import igrek.songbook.system.SoftKeyboardService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File


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

    private val logger = LoggerFactory.logger

    private val cmdRules: List<CommandRule> by lazy {
        listOf(
                CommandRule({
                    it.matches("^m[ou]+$".toRegex())
                }) { showCowSuperPowers() },
                SimplifiedKeyRule("dupa", "okon") { showCowSuperPowers() },
                SimplifiedKeyRule("lich", "lisz") { toast("\"Trup tu tupta...\"") },

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

                SubCommandRule("login") { key: String ->
                    this.adminService.loginAdmin(key)
                },

                CommandRule({
                    it.trim().matches(encodedSecretRegex)
                }) { decodeSecretKeys(it) },

                SubCommandRule("hush", ::hashedCommand),
                SubCommandRule("shell") { shellCommand(it, showStdout = false) },
                SubCommandRule("shellout") { shellCommand(it, showStdout = true) },
                SubCommandRule("unlock", ::unlockSongs),
                SubCommandRule("backup local", ::backupDataFiles),
                SubCommandRule("restore local", ::restoreDataFiles),
                SubCommandRule("exit now") {
                    GlobalScope.launch(Dispatchers.Main) {
                        toast("exiting...")
                        this@SecretCommandService.activityController.quit()
                    }
                },
        )
    }

    private fun backupDataFiles(cmd: String) {
        runCatching {
            val parts = cmd.split(" ", limit = 2)
            check(parts.size == 2) { "insufficient sections" }
            val dataDirPath = localDbService.appFilesDir.absolutePath
            val localSrcFile = File(dataDirPath, parts[0])
            val localDstFile = File(parts[1])
            localSrcFile.copyTo(localDstFile)
            toast("File copied from $localSrcFile to $localDstFile")
        }.onFailure { t ->
            val message = t.message.orEmpty() + "\nType: ${t::class.simpleName}"
            showDialog("Error", message)
        }
    }

    private fun restoreDataFiles(cmd: String) {
        runCatching {
            val parts = cmd.split(" ", limit = 2)
            check(parts.size == 2) { "insufficient sections" }
            val dataDirPath = localDbService.appFilesDir.absolutePath
            val localSrcFile = File(parts[0])
            val localDstFile = File(dataDirPath, parts[1])
            localSrcFile.copyTo(localDstFile, overwrite = true)
            toast("File copied from $localSrcFile to $localDstFile")
        }.onFailure { t ->
            val message = t.message.orEmpty() + "\nType: ${t::class.simpleName}"
            showDialog("Error", message)
        }
    }

    private val encodedSecretRegex = Regex("""---SONGBOOK-KEY---([\S\s]+?)---SONGBOOK-KEY---""")

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
        try {
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
        } catch (t: Throwable) {
            logger.error("JWT decoding error", t)
        }
    }

    private val hashedCommands: Map<String, () -> Unit> by lazy {
        mapOf(
                "5581b03c159338d1e17cdc04c424788209a4c52cfa65c981af93de3a0600a427" to { disableAds() }
        )
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

        GlobalScope.launch {
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
            logger.warn("invalid hashed command entered: $cmd (#$hash)")
            return
        }
        logger.info("hashed command activated: $cmd (#$hash)")
        val command = hashedCommands[hash]
        command?.invoke()
    }

    private fun shellCommand(cmd: String, showStdout: Boolean = false) {
        logger.debug("Running shell command: $cmd")
        try {
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
                        showDialog("Command successful", "$stdout\n$stderr")
                    } else {
                        showDialog("Command failed", "$stdout\n$stderr\nerror code: $retCode")
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
        } catch (t: Throwable) {
            toast("command error: ${t.message}")
            logger.error("command error", t)
        }
    }

    private fun showDialog(title: String, stdout: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val alertBuilder = AlertDialog.Builder(activity)
                    .setMessage(stdout)
                    .setTitle(title)
                    .setPositiveButton(uiResourceService.resString(R.string.action_info_ok)) { _, _ -> }
                    .setCancelable(true)
            val alertDialog = alertBuilder.create()
            if (!activity.isFinishing) {
                alertDialog.show()
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
                .filter { s -> s.lockPassword == key }.count()
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