package igrek.songbook.admin

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.admin.antechamber.AntechamberService
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.system.SoftKeyboardService
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class AdminService {
    @Inject
    lateinit var preferencesService: Lazy<PreferencesService>
    @Inject
    lateinit var navigationMenuController: Lazy<NavigationMenuController>
    @Inject
    lateinit var preferencesState: PreferencesState
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var antechamberService: AntechamberService

    @Inject
    lateinit var activity: Activity

    @Inject
    lateinit var softKeyboardService: SoftKeyboardService

    var userAuthToken: String
        get() = preferencesState.userAuthToken
        set(value) {
            preferencesState.userAuthToken = value
        }

    fun login(key: String) {
        val match = Regex("login (.*)").matchEntire(key)
        match?.let {
            userAuthToken = it.groupValues[1]
            LoggerFactory.logger.debug("Admin token entered: [$userAuthToken]")
            checkMenuVisibility()
        }
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun init() {
        checkMenuVisibility()
    }

    private fun checkMenuVisibility() {
        if (isAdminEnabled()) {
            LoggerFactory.logger.debug("Enabling admin tools")
            navigationMenuController.get().setAdminMenu()
        }
    }

    fun isAdminEnabled(): Boolean {
        return userAuthToken.isNotBlank()
    }

    fun updatePublicSongUi(song: Song) {
        uiInfoService.showInfoIndefinite(R.string.admin_sending)
        antechamberService.updatePublicSong(song)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    uiInfoService.showInfo(R.string.admin_success)
                }, { error ->
                    val message = uiResourceService.resString(R.string.admin_communication_breakdown, error.message)
                    uiInfoService.showInfoIndefinite(message)
                })
    }

    fun updateRankDialog(song: Song) {
        val secretTitle = uiResourceService.resString(R.string.action_secret)
        val dlgAlert = AlertDialog.Builder(activity)
        dlgAlert.setMessage(uiResourceService.resString(R.string.unlock_type_in_secret_key))
        dlgAlert.setTitle(secretTitle)

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(song.rank?.toString().orEmpty())
        dlgAlert.setView(input)

        val actionCheck = uiResourceService.resString(R.string.action_check_secret)
        dlgAlert.setPositiveButton(actionCheck) { _, _ ->
            val newRank = input.text.toString().toDouble()
            updateRank(song, newRank)
        }
        dlgAlert.setNegativeButton(uiResourceService.resString(R.string.action_cancel)) { _, _ -> }
        dlgAlert.setCancelable(true)
        dlgAlert.create().show()

        input.requestFocus()
        Handler(Looper.getMainLooper()).post {
            softKeyboardService.showSoftKeyboard(input)
        }
    }

    private fun updateRank(song: Song, rank: Double?) {
        // TODO push request
    }
}