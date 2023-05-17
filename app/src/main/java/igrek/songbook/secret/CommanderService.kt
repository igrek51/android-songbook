package igrek.songbook.secret

import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.WindowManager.LayoutParams
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.admin.AdminService
import igrek.songbook.billing.BillingLayoutController
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.info.logview.LogsLayoutController
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.room.RoomListLayoutController
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.system.SoftKeyboardService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class CommanderService(
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
    preferencesService: LazyInject<PreferencesService> = appFactory.preferencesService,
    adminService: LazyInject<AdminService> = appFactory.adminService,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    userDataDao: LazyInject<UserDataDao> = appFactory.userDataDao,
    commanderUtils: LazyInject<CommanderUtils> = appFactory.commanderUtils,
) {
    private val activity by LazyExtractor(appCompatActivity)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val preferencesService by LazyExtractor(preferencesService)
    private val adminService by LazyExtractor(adminService)
    private val activityController by LazyExtractor(activityController)
    private val layoutController by LazyExtractor(layoutController)
    private val userDataDao by LazyExtractor(userDataDao)
    private val commanderUtils by LazyExtractor(commanderUtils)

    private val logger = LoggerFactory.logger

    private val cmdRules: List<CommandRule> by lazy {
        listOf(
            CommandRule({
                it.matches("^m[ou]+$".toRegex())
            }) { this.commanderUtils.showCowSuperPowers() },
            SimpleKeyRule("dupa", "okon") { this.commanderUtils.showCowSuperPowers() },

            CommandRule({ it.trim().matches(encodedSecretRegex) }) {
                decodeJwtKeys(it)
            },

            SubCommandRule("hush", ::runHashedCommand),

            SimpleKeyRule("logs") {
                this.layoutController.showLayout(LogsLayoutController::class)
            },

            SimpleKeyRule("exit now") {
                GlobalScope.launch(Dispatchers.Main) {
                    uiInfoService.get().showToast("exiting...")
                    this@CommanderService.activityController.quitImmediately()
                }
            },

            SubCommandRule("shell") { this.commanderUtils.shellCommand(it, showStdout = false) },
            SubCommandRule("shellout") { this.commanderUtils.shellCommand(it, showStdout = true) },

            SubCommandRule("unlock", this.commanderUtils::unlockSongs),
            SimpleKeyRule("engineer", "inzynier") { this.commanderUtils.unlockSongs("engineer") },

            SimpleKeyRule("grant permission storage") {
                this.commanderUtils.grantStoragePermission()
            },

            // backup from /data/data/PACKAGE/files/:
            // appdata backup local customsongs.1.json /sdcard/Android/data/igrek.songbook/files/customsongs.json
            SubCommandRule("appdata backup local", this.commanderUtils::backupAppDataLocalFile),
            // appdata restore local /sdcard/customsongs.json customsongs.1.json
            SubCommandRule("appdata restore local", this.commanderUtils::restoreAppDataLocalFile),
            // appdata backup dialog customsongs.1.json
            SubCommandRule("appdata backup dialog", this.commanderUtils::backupAppDataDialog),
            // appdata restore dialog customsongs.1.json
            SubCommandRule("appdata restore dialog", this.commanderUtils::restoreAppDataDialog),
            // appdata edit customsongs.1.json
            SubCommandRule("appdata edit", this.commanderUtils::editAppDataDialog),

            SimpleKeyRule("reset") {
                this.songsRepository.fullFactoryReset()
                this.preferencesService.clear()
                success("Factory reset done")
            },
            SimpleKeyRule("reset config") {
                this.preferencesService.clear()
                success("Factory reset: Settings")
            },
            SimpleKeyRule("reset songs") {
                this.songsRepository.resetGeneralSongsData()
                this.songsRepository.reloadSongsDb()
                success("Factory reset: Public songs")
            },
            SimpleKeyRule("reset user") {
                this.userDataDao.factoryReset()
                this.songsRepository.reloadSongsDb()
                success("Factory reset: User data")
            },
            SimpleKeyRule("reload songs") {
                this.songsRepository.reloadSongsDb()
            },

            SimpleKeyRule("firebase crashme") {
                logger.error(IllegalArgumentException("real reason"))
                throw RuntimeException("deliberate disaster")
            },
            SimpleKeyRule("firebase logs") {
                logger.error(IllegalArgumentException("real reason"))
                logger.error("error log")
                appFactory.crashlyticsLogger.get().sendCrashlyticsAsync()
            },
            SimpleKeyRule("firebase error") {
                appFactory.crashlyticsLogger.get()
                    .reportNonFatalError(IllegalArgumentException("something bad"))
            },
            SimpleKeyRule("firebase send") {
                appFactory.crashlyticsLogger.get().sendCrashlyticsAsync()
            },

            SubCommandRule("login") { key: String ->
                this.adminService.loginAdmin(key)
            },

            SimpleKeyRule("test") {
                logger.debug("waiting until initialized")
                appFactory.appInitializer.get().waitUntilInitialized()
                success("initialized")
            },

            SimpleKeyRule("ad show") { this.commanderUtils.enableAds() },

            SimpleKeyRule("device id") { this.commanderUtils.showDeviceId() },

            SimpleKeyRule("goto shop") {
                this.layoutController.showLayout(BillingLayoutController::class)
            },
            SimpleKeyRule("goto bt-share") {
                this.layoutController.showLayout(RoomListLayoutController::class)
            },
        )
    }

    private val hashedCommands: Map<String, () -> Unit> by lazy {
        mapOf(
            "5581b03c159338d1e17cdc04c424788209a4c52cfa65c981af93de3a0600a427" to { this.commanderUtils.disableAds() }
        )
    }

    private val encodedSecretRegex by lazy { Regex("""---SONGBOOK-KEY---([\S\s]+?)---SONGBOOK-KEY---""") }


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
        dlgAlert.setPositiveButton(actionCheck) { _, _ ->
            commandAttempt(input.text.toString())
        }
        dlgAlert.setNegativeButton(uiResourceService.resString(R.string.action_cancel)) { _, _ -> }
        dlgAlert.setCancelable(true)
        val dialog = dlgAlert.create()
        dialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()

        input.requestFocus()
        Handler(Looper.getMainLooper()).post {
            softKeyboardService.showSoftKeyboard(input)
        }
    }

    private fun commandAttempt(key: String) {
        logger.info("secret command entered: $key")

        GlobalScope.launch(Dispatchers.Main) {
            safeExecute {
                val trimmedKey = key.trim()
                runActivationRules(trimmedKey)
            }
        }

        softKeyboardService.hideSoftKeyboard()
    }

    private suspend fun runActivationRules(command: String) {
        val multiCommands = command.split(";")
        if (multiCommands.size > 1) {
            multiCommands.forEach { subcommand ->
                runActivationRules(subcommand.trim())
            }
            return
        }

        val activation = findActivator(cmdRules, command)

        if (activation == null) {
            val errorMessage = uiResourceService.resString(R.string.secret_key_invalid, command)
            throw RuntimeException(errorMessage)
        }

        logger.debug("Command activated: $command")
        activation.run()
    }

    private fun runHashedCommand(cmd: String) {
        val hash = ShaHasher().hash(cmd)
        if (hash !in hashedCommands) {
            logger.warn("invalid hashed command: $cmd (#$hash)")
            throw RuntimeException("invalid \"hush\" command (#$hash)")
        }
        success("hashed command activated: $cmd (#$hash)")
        val command = hashedCommands[hash]
        command?.invoke()
    }

    private suspend fun decodeJwtKeys(key: String) {
        val match = encodedSecretRegex.matchEntire(key.trim())
        match?.let {
            val encodedCommands = it.groupValues[1].trim().lines()
            encodedCommands.forEach { encCmd ->
                decodeJwtKey(encCmd.trim())
            }
        }
    }

    private suspend fun decodeJwtKey(skey: String) {
        logger.debug("decoding JWT secret key: $skey")
        safeExecute {
            val jwt = JWT(skey)
            when {
                "cmd" in jwt.claims -> {
                    jwt.claims["cmd"]?.asString()?.let { cmd ->
                        logger.debug("JWT decoded cmd: $cmd")
                        runActivationRules(cmd)
                    }
                }
                "cmds" in jwt.claims -> {
                    jwt.claims["cmds"]?.asList(String::class.java)?.let { cmds ->
                        logger.debug("JWT decoded cmds: $cmds")
                        cmds.forEach { cmd ->
                            runActivationRules(cmd)
                        }
                    }
                }
            }
        }
    }

    private fun success(message: String) {
        commanderUtils.success(message)
    }

}
