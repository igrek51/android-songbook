package igrek.songbook.service.secret

import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.widget.EditText
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.logger.LoggerFactory
import igrek.songbook.service.info.UiInfoService
import igrek.songbook.service.info.UiResourceService
import igrek.songbook.service.persistence.SongsDbRepository
import igrek.songbook.service.persistence.database.SqlQueryService
import java.util.*
import javax.inject.Inject

class SecretUnlockerService {

    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var songsDbRepository: SongsDbRepository
    @Inject
    lateinit var sqlQueryService: SqlQueryService

    private val locale = Locale("pl", "PL")
    private val logger = LoggerFactory.getLogger()

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun showUnlockAlert() {
        val unlockAction = uiResourceService.resString(R.string.unlock_action)
        val dlgAlert = AlertDialog.Builder(activity)
        dlgAlert.setMessage("Type in a secret key:")
        dlgAlert.setTitle(unlockAction)

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT
        dlgAlert.setView(input)

        dlgAlert.setPositiveButton(unlockAction) { _, _ -> unlockAttempt(input.text.toString()) }
        dlgAlert.setNegativeButton(uiResourceService.resString(R.string.action_cancel)) { _, _ -> }
        dlgAlert.setCancelable(true)
        dlgAlert.create().show()
    }

    private fun unlockAttempt(key0: String) {
        logger.info("unlocking attempt with a key: $key0")
        val key = key0.toLowerCase(locale)
        when (key) {
            "dupa", "okoń", "okon" -> uiInfoService.showInfo("Congratulations! You have discovered an Easter Egg :)")
            "engineer", "inżynier", "inzynier" -> unlockSongs("engineer")
            "zjajem", "z jajem" -> unlockSongs("zjajem")
            "bff" -> unlockSongs("bff")
            "religijne" -> unlockSongs("religijne")
            else -> {
                uiInfoService.showToast(R.string.unlock_key_invalid)
            }
        }
    }

    private fun unlockSongs(key: String) {
        val toUnlock = songsDbRepository.songsDb!!.allSongs
                .filter { s -> s.locked && s.lockPassword == key }
        val count = toUnlock.count()
        toUnlock.forEach { s ->
            s.locked = false
            sqlQueryService.unlockSong(s.id)
        }
        val message = uiResourceService.resString(R.string.unlock_new_songs_unlocked) + count
        uiInfoService.showInfo(message)
        songsDbRepository.reloadDb()
    }
}
