package igrek.songbook.about.secret

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.common.base.Predicate
import com.google.firebase.crashlytics.FirebaseCrashlytics
import igrek.songbook.R
import igrek.songbook.admin.AdminService
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.ad.AdService
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.system.SoftKeyboardService
import igrek.songbook.system.locale.StringSimplifier
import javax.inject.Inject


class SecretCommandService {

    @Inject
    lateinit var activity: AppCompatActivity

    @Inject
    lateinit var uiResourceService: UiResourceService

    @Inject
    lateinit var uiInfoService: UiInfoService

    @Inject
    lateinit var songsRepository: SongsRepository

    @Inject
    lateinit var softKeyboardService: SoftKeyboardService

    @Inject
    lateinit var preferencesService: PreferencesService

    @Inject
    lateinit var adminService: AdminService

    @Inject
    lateinit var adService: AdService

    private val logger = LoggerFactory.logger

    private val cowCondition: Predicate<String> = Predicate {
        it?.matches("^m[ou]+$".toRegex()) ?: false
    }
    private val dupaCondition: Predicate<String> = Predicate { it?.toLowerCase() == "dupa" }

    private val rules: List<CommandRule> by lazy {
        listOf(
                CommandRule(dupaCondition) { showCowSuperPowers() },
                CommandRule(cowCondition) { showCowSuperPowers() },
                CommandRule("okon") { showCowSuperPowers() },
                CommandRule("lich", "lisz") { toast("\"Trup tu tupta...\"") },

                CommandRule("engineer", "inzynier") { unlockSongs("engineer") },
                CommandRule("zjajem", "z jajem") { unlockSongs("zjajem") },
                CommandRule("afcg") { unlockSongs("afcg") },

                CommandRule("reset") {
                    songsRepository.factoryReset()
                    preferencesService.clear()
                },
                CommandRule("reset config") { preferencesService.clear() },
                CommandRule("reset db") { songsRepository.factoryReset() },
                CommandRule("reset db general") {
                    songsRepository.resetGeneralData()
                    songsRepository.reloadSongsDb()
                },
                CommandRule("reset db user") {
                    songsRepository.resetUserData()
                    songsRepository.reloadSongsDb()
                },

                CommandRule("firebase crashme") {
                    logger.error(IllegalArgumentException("real reason"))
                    throw RuntimeException("deliberate disaster")
                },
                CommandRule("firebase error") {
                    logger.error(IllegalArgumentException("real reason"))
                    logger.error("error log")
                    FirebaseCrashlytics.getInstance().sendUnsentReports()
                },

                CommandRule(Predicate {
                    it?.startsWith("login ") ?: false
                }) { key: String ->
                    adminService.login(key)
                },

                CommandRule("ad show") {
                    adService.enableAds()
                },

                CommandRule(Predicate {
                    it?.startsWith("hush ") ?: false
                }) { key: String ->
                    hashedCommand(key.drop(5))
                },

                SubCommandRule("unlock") { key -> unlockSongs(key) },
        )
    }

    private val hashedCommands: Map<String, () -> Unit> by lazy {
        mapOf(
                "5581b03c159338d1e17cdc04c424788209a4c52cfa65c981af93de3a0600a427" to { disableAds() }
        )
    }

    private fun hashedCommand(cmd: String) {
        val hash = ShaHasher().hash(cmd)
        if (hash !in hashedCommands) {
            logger.warn("invalid hashed command entered: $cmd (#$hash)")
            return
        }
        logger.info("hashed command entered: $cmd (#$hash)")
        val command = hashedCommands[hash]
        command?.invoke()
    }

    private fun disableAds() {
        adService.disableAds()
        toast(R.string.ads_disabled)
    }

    @SuppressLint("InflateParams")
    private fun showCowSuperPowers() {
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

        dialog.show()

        toast(R.string.easter_egg_discovered)
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun showUnlockAlert() {
        val secretTitle = uiResourceService.resString(R.string.action_secret)
        val dlgAlert = AlertDialog.Builder(activity)
        dlgAlert.setMessage(uiResourceService.resString(R.string.unlock_type_in_secret_key))
        dlgAlert.setTitle(secretTitle)

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT
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

    private fun commandAttempt(key0: String) {
        logger.info("command attempt with a key: $key0")
        val key = StringSimplifier.simplify(key0)

        if (!checkActivationRules(key)) {
            toast(R.string.unlock_key_invalid)
        }

        softKeyboardService.hideSoftKeyboard()
    }

    private fun checkActivationRules(key: String): Boolean {
        for (rule in rules) {
            if (rule.condition.apply(key)) {
                logger.debug("rule activated: $key")
                rule.activator(key)
                return true
            }
        }
        return false
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
