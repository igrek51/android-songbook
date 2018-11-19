package igrek.songbook.about.secret

import android.content.Context
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import com.google.common.base.Predicate
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.SongsRepository
import igrek.songbook.persistence.preferences.PreferencesService
import igrek.songbook.system.SoftKeyboardService
import igrek.songbook.system.locale.StringSimplifier
import javax.inject.Inject


class SecretUnlockerService {

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

    private val logger = LoggerFactory.getLogger()

    private val cowCondition: Predicate<String> = Predicate { input -> input!!.matches(Regex("^m[ou]+$")) }

    private val EA5T3R_M00: String = """
 _____________________
/ Congratulations!    \
|                     |
| You have discovered |
\ an Easter Egg :)    /
 ---------------------
   \   ^__^
    \  (oo)\_______
       (__)\       )\/\
           ||----w |
           ||     ||
"""

    private val rules: List<UnlockerRule> = listOf(
            UnlockerRule("dupa", "okon") { showCowSuperPowers() },
            UnlockerRule("arthas") { toast("\"Nie trzeba mi się kłaniać.\"") },
            UnlockerRule("lich", "lisz") { toast("\"Trup tu tupta...\"") },
            UnlockerRule(cowCondition) { showCowSuperPowers() },

            UnlockerRule("engineer", "inzynier") { unlockSongs("engineer") },
            UnlockerRule("bff") { unlockSongs("bff") },
            UnlockerRule("zjajem", "z jajem") { unlockSongs("zjajem") },
            UnlockerRule("religijne") { unlockSongs("religijne") },

            UnlockerRule("reset") { reset() }
    )

    private fun showCowSuperPowers() {
        val alertBuilder = AlertDialog.Builder(activity)
        alertBuilder.setTitle("Moooo!")
        alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_info_ok)) { _, _ -> }
        alertBuilder.setCancelable(true)
        val dialog: AlertDialog = alertBuilder.create()

        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = inflater.inflate(R.layout.alert_monospace, null, false)
        val contentTextView = itemView.findViewById(R.id.contentTextView) as TextView
        contentTextView.text = EA5T3R_M00
        contentTextView.isVerticalScrollBarEnabled = true
        dialog.setView(itemView)

        dialog.show()

        toast(R.string.easter_egg_discovered)
    }

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun showUnlockAlert() {
        val unlockAction = uiResourceService.resString(R.string.action_unlock)
        val dlgAlert = AlertDialog.Builder(activity)
        dlgAlert.setMessage(uiResourceService.resString(R.string.unlock_type_in_secret_key))
        dlgAlert.setTitle(unlockAction)

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dlgAlert.setView(input)

        dlgAlert.setPositiveButton(unlockAction) { _, _ -> unlockAttempt(input.text.toString()) }
        dlgAlert.setNegativeButton(uiResourceService.resString(R.string.action_cancel)) { _, _ -> }
        dlgAlert.setCancelable(true)
        dlgAlert.create().show()

        Handler().post {
            softKeyboardService.showSoftKeyboard(input)
        }
    }

    private fun unlockAttempt(key0: String) {
        logger.info("unlocking attempt with a key: $key0")
        val key = StringSimplifier.simplify(key0)

        if (!checkActivationRules(key)) {
            toast(R.string.unlock_key_invalid)
        }

        softKeyboardService.hideSoftKeyboard()
    }

    private fun checkActivationRules(key: String): Boolean {
        for (rule in rules) {
            if (rule.condition.apply(key)) {

                rule.activator()

                return true
            }
        }
        return false
    }

    private fun reset() {
        songsRepository.factoryReset()
        preferencesService.clear()
    }

    private fun toast(message: String) {
        uiInfoService.showToast(message)
    }

    private fun toast(resId: Int) {
        uiInfoService.showToast(resId)
    }

    private fun unlockSongs(key: String) {
        val toUnlock = songsRepository.songsDb!!.allSongs
                .filter { s -> s.locked && s.lockPassword == key }
        val count = toUnlock.count()
        toUnlock.forEach { s ->
            s.locked = false
        }
        songsRepository.unlockKey(key)
        val message = uiResourceService.resString(R.string.unlock_new_songs_unlocked, count)
        uiInfoService.showToast(message)
        songsRepository.initializeSongsDb()
    }
}
