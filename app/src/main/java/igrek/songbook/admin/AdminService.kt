package igrek.songbook.admin

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import igrek.songbook.R
import igrek.songbook.admin.antechamber.AntechamberService
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.system.SoftKeyboardService
import io.reactivex.android.schedulers.AndroidSchedulers

class AdminService(
        navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
        preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
        antechamberService: LazyInject<AntechamberService> = appFactory.antechamberService,
        activity: LazyInject<Activity> = appFactory.activity,
        softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
        songRankService: LazyInject<SongRankService> = appFactory.songRankService,
        adminCategoryManager: LazyInject<AdminCategoryManager> = appFactory.adminCategoryManager,
) {
    private val navigationMenuController by LazyExtractor(navigationMenuController)
    private val preferencesState by LazyExtractor(preferencesState)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val antechamberService by LazyExtractor(antechamberService)
    private val activity by LazyExtractor(activity)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val songRankService by LazyExtractor(songRankService)
    private val adminCategoryManager by LazyExtractor(adminCategoryManager)

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

    fun init() {
        checkMenuVisibility()
    }

    private fun checkMenuVisibility() {
        if (isAdminEnabled()) {
            LoggerFactory.logger.debug("Enabling admin tools")
            navigationMenuController.setAdminMenu()
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
        val dlgAlert = AlertDialog.Builder(activity)
        dlgAlert.setMessage(uiResourceService.resString(R.string.admin_update_rank))

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText(song.rank?.toString().orEmpty())
        dlgAlert.setView(input)

        val actionCheck = uiResourceService.resString(R.string.action_info_ok)
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

    fun createCategoryDialog() {
        val dlgAlert = AlertDialog.Builder(activity)
        dlgAlert.setMessage(uiResourceService.resString(R.string.admin_update_rank))

        val input = EditText(activity).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        dlgAlert.setView(input)

        val actionCheck = uiResourceService.resString(R.string.action_info_ok)
        dlgAlert.setPositiveButton(actionCheck) { _, _ ->
            val categoryName = input.text.toString()
            createCategory(categoryName)
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
        uiInfoService.showInfoIndefinite(R.string.admin_sending)
        songRankService.updateRank(song, rank)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    uiInfoService.showInfo(R.string.admin_success)
                }, { error ->
                    val message = uiResourceService.resString(R.string.admin_communication_breakdown, error.message)
                    uiInfoService.showInfoIndefinite(message)
                })
    }

    private fun createCategory(categoryName: String) {
        uiInfoService.showInfoIndefinite(R.string.admin_sending)
        adminCategoryManager.createCategory(categoryName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    uiInfoService.showInfo(R.string.admin_success)
                }, { error ->
                    val message = uiResourceService.resString(R.string.admin_communication_breakdown, error.message)
                    uiInfoService.showInfoIndefinite(message)
                })
    }
}