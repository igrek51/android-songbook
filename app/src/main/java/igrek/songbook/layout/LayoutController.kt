package igrek.songbook.layout

import android.app.Activity
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.admin.antechamber.AdminSongsLayoutContoller
import igrek.songbook.custom.CustomSongsListLayoutController
import igrek.songbook.custom.EditSongLayoutController
import igrek.songbook.editor.ChordsEditorLayoutController
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.ad.AdService
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.send.ContactLayoutController
import igrek.songbook.send.MissingSongLayoutController
import igrek.songbook.send.PublishSongLayoutController
import igrek.songbook.settings.SettingsLayoutController
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songselection.favourite.FavouritesLayoutController
import igrek.songbook.songselection.history.OpenHistoryLayoutController
import igrek.songbook.songselection.latest.LatestSongsLayoutController
import igrek.songbook.songselection.search.SongSearchLayoutController
import igrek.songbook.songselection.top.TopSongsLayoutController
import igrek.songbook.songselection.tree.SongTreeLayoutController
import kotlin.reflect.KClass

class LayoutController(
        activity: LazyInject<Activity> = appFactory.activity,
        navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
        activityController: LazyInject<ActivityController> = appFactory.activityController,
        adService: LazyInject<AdService> = appFactory.adService,
        songTreeLayoutController: LazyInject<SongTreeLayoutController> = appFactory.songTreeLayoutController,
        songSearchLayoutController: LazyInject<SongSearchLayoutController> = appFactory.songSearchLayoutController,
        songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
        contactLayoutController: LazyInject<ContactLayoutController> = appFactory.contactLayoutController,
        settingsLayoutController: LazyInject<SettingsLayoutController> = appFactory.settingsLayoutController,
        editSongLayoutController: LazyInject<EditSongLayoutController> = appFactory.editSongLayoutController,
        chordsEditorLayoutController: LazyInject<ChordsEditorLayoutController> = appFactory.chordsEditorLayoutController,
        customSongsListLayoutController: LazyInject<CustomSongsListLayoutController> = appFactory.customSongsListLayoutController,
        favouritesLayoutController: LazyInject<FavouritesLayoutController> = appFactory.favouritesLayoutController,
        playlistLayoutController: LazyInject<PlaylistLayoutController> = appFactory.playlistLayoutController,
        latestSongsLayoutController: LazyInject<LatestSongsLayoutController> = appFactory.latestSongsLayoutController,
        topSongsLayoutController: LazyInject<TopSongsLayoutController> = appFactory.topSongsLayoutController,
        openHistoryLayoutController: LazyInject<OpenHistoryLayoutController> = appFactory.openHistoryLayoutController,
        missingSongLayoutController: LazyInject<MissingSongLayoutController> = appFactory.missingSongLayoutController,
        publishSongLayoutController: LazyInject<PublishSongLayoutController> = appFactory.publishSongLayoutController,
        adminSongsLayoutContoller: LazyInject<AdminSongsLayoutContoller> = appFactory.adminSongsLayoutContoller,
) {
    private val activity by LazyExtractor(activity)
    private val navigationMenuController by LazyExtractor(navigationMenuController)
    private val activityController by LazyExtractor(activityController)
    private val adService by LazyExtractor(adService)
    private val songTreeLayoutController by LazyExtractor(songTreeLayoutController)
    private val songSearchLayoutController by LazyExtractor(songSearchLayoutController)
    private val songPreviewLayoutController by LazyExtractor(songPreviewLayoutController)
    private val contactLayoutController by LazyExtractor(contactLayoutController)
    private val settingsLayoutController by LazyExtractor(settingsLayoutController)
    private val editSongLayoutController by LazyExtractor(editSongLayoutController)
    private val chordsEditorLayoutController by LazyExtractor(chordsEditorLayoutController)
    private val customSongsListLayoutController by LazyExtractor(customSongsListLayoutController)
    private val favouritesLayoutController by LazyExtractor(favouritesLayoutController)
    private val playlistLayoutController by LazyExtractor(playlistLayoutController)
    private val latestSongsLayoutController by LazyExtractor(latestSongsLayoutController)
    private val topSongsLayoutController by LazyExtractor(topSongsLayoutController)
    private val openHistoryLayoutController by LazyExtractor(openHistoryLayoutController)
    private val missingSongLayoutController by LazyExtractor(missingSongLayoutController)
    private val publishSongLayoutController by LazyExtractor(publishSongLayoutController)
    private val adminSongsLayoutContoller by LazyExtractor(adminSongsLayoutContoller)

    private lateinit var mainContentLayout: CoordinatorLayout
    private var currentLayout: MainLayout? = null
    private var layoutHistory: MutableList<MainLayout> = mutableListOf()
    private var registeredLayouts: Map<KClass<out MainLayout>, MainLayout> = emptyMap()
    private val logger = LoggerFactory.logger

    fun init() {
        activity.setContentView(R.layout.main_layout)
        mainContentLayout = activity.findViewById(R.id.main_content)
        navigationMenuController.init()
        registerLayouts()
    }

    private fun registerLayouts() {
        registeredLayouts = mapOf(
                SongTreeLayoutController::class to songTreeLayoutController,
                SongSearchLayoutController::class to songSearchLayoutController,
                SongPreviewLayoutController::class to songPreviewLayoutController,
                ContactLayoutController::class to contactLayoutController,
                SettingsLayoutController::class to settingsLayoutController,
                EditSongLayoutController::class to editSongLayoutController,
                ChordsEditorLayoutController::class to chordsEditorLayoutController,
                CustomSongsListLayoutController::class to customSongsListLayoutController,
                FavouritesLayoutController::class to favouritesLayoutController,
                PlaylistLayoutController::class to playlistLayoutController,
                LatestSongsLayoutController::class to latestSongsLayoutController,
                TopSongsLayoutController::class to topSongsLayoutController,
                OpenHistoryLayoutController::class to openHistoryLayoutController,
                MissingSongLayoutController::class to missingSongLayoutController,
                PublishSongLayoutController::class to publishSongLayoutController,
                AdminSongsLayoutContoller::class to adminSongsLayoutContoller,
        )
    }

    fun showLayout(layoutClass: KClass<out MainLayout>, disableReturn: Boolean = false) {
        val layoutController = registeredLayouts[layoutClass]
                ?: throw IllegalArgumentException("${layoutClass.simpleName} class not registered as layout")

        if (disableReturn) {
            // remove current layout from history
            if (currentLayout in layoutHistory) {
                layoutHistory.remove(currentLayout)
            }
        }

        layoutController.let {
            if (it in layoutHistory) {
                layoutHistory.remove(it)
            }
            layoutHistory.add(it)
        }

        logger.debug("Showing layout ${layoutClass.simpleName} [${layoutHistory.size} in history]")
        showMainLayout(layoutController)
    }

    private fun showMainLayout(mainLayout: MainLayout) {
        currentLayout?.onLayoutExit()
        currentLayout = mainLayout

        val inflater = activity.layoutInflater
        val properLayoutView = inflater.inflate(mainLayout.getLayoutResourceId(), null)
        properLayoutView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mainContentLayout.removeAllViews()
        mainContentLayout.addView(properLayoutView)

        mainLayout.showLayout(properLayoutView)
        postInitLayout(mainLayout)
    }

    private fun postInitLayout(currentLayout: MainLayout) {
        adService.updateAdBanner(currentLayout)
    }

    fun showPreviousLayoutOrQuit() {
        // remove current layout from last place
        try {
            val last = layoutHistory.last()
            if (last == currentLayout) {
                layoutHistory = layoutHistory.dropLast(1).toMutableList()
            }
        } catch (e: NoSuchElementException) {
        }

        if (layoutHistory.isEmpty()) {
            activityController.quit()
            return
        }

        val previousLayout = layoutHistory.last()
        logger.debug("Showing previous layout ${previousLayout::class.simpleName} [${layoutHistory.size} in history]")
        showMainLayout(previousLayout)
    }

    fun isState(compareLayoutClass: KClass<out MainLayout>): Boolean {
        return compareLayoutClass.isInstance(currentLayout)
    }

    fun onBackClicked() {
        if (navigationMenuController.isDrawerShown()) {
            navigationMenuController.navDrawerHide()
            return
        }
        SafeExecutor {
            currentLayout?.onBackClicked()
        }
    }

}
