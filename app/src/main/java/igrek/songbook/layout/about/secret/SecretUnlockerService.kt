package igrek.songbook.layout.about.secret

import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.widget.EditText
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
        when (key) {
            "dupa", "okon" -> toast(R.string.easter_egg_discovered)
            "engineer", "inzynier" -> unlockSongs("engineer")
            "bff" -> unlockSongs("bff")
            "zjajem", "z jajem" -> unlockSongs("zjajem")
            "religijne" -> unlockSongs("religijne")
            "arthas" -> toast("\"Nie trzeba mi się kłaniać.\"")
            "lich", "lisz" -> toast("\"Trup tu tupta...\"")
            "reset" -> reset()
            else -> {
                toast(R.string.unlock_key_invalid)
            }
        }
        softKeyboardService.hideSoftKeyboard()
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
            songsRepository.unlockSong(s.id)
        }
        val message = uiResourceService.resString(R.string.unlock_new_songs_unlocked, count)
        uiInfoService.showToast(message)
        songsRepository.initializeSongsDb()
    }
}
