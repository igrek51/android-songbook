package igrek.songbook.layout

import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.Transition
import androidx.transition.TransitionManager
import igrek.songbook.R
import igrek.songbook.about.WebviewLayoutController
import igrek.songbook.activity.ActivityController
import igrek.songbook.admin.antechamber.AdminSongsLayoutContoller
import igrek.songbook.billing.BillingLayoutController
import igrek.songbook.custom.CustomSongsListLayoutController
import igrek.songbook.custom.EditSongLayoutController
import igrek.songbook.editor.ChordsEditorLayoutController
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.ad.AdService
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.playlist.PlaylistFillLayoutController
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.room.RoomListLayoutController
import igrek.songbook.room.RoomLobbyLayoutController
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
import igrek.songbook.system.SystemKeyDispatcher
import kotlinx.coroutines.*
import kotlin.reflect.KClass


@OptIn(DelicateCoroutinesApi::class)
class LayoutController(
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
    navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    adService: LazyInject<AdService> = appFactory.adService,
    systemKeyDispatcher: LazyInject<SystemKeyDispatcher> = appFactory.systemKeyDispatcher,
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
    roomListLayoutController: LazyInject<RoomListLayoutController> = appFactory.shareViewLayoutController,
    roomLobbyLayoutController: LazyInject<RoomLobbyLayoutController> = appFactory.roomLobbyLayoutController,
    billingLayoutController: LazyInject<BillingLayoutController> = appFactory.billingLayoutController,
    webviewLayoutController: LazyInject<WebviewLayoutController> = appFactory.webviewLayoutController,
    playlistFillLayoutController: LazyInject<PlaylistFillLayoutController> = appFactory.playlistFillLayoutController,
) {
    private val activity by LazyExtractor(appCompatActivity)
    private val navigationMenuController by LazyExtractor(navigationMenuController)
    private val activityController by LazyExtractor(activityController)
    private val adService by LazyExtractor(adService)
    private val systemKeyDispatcher by LazyExtractor(systemKeyDispatcher)

    private lateinit var mainContentLayout: CoordinatorLayout
    private var currentLayout: MainLayout? = null
    private var layoutHistory: MutableList<MainLayout> = mutableListOf()
    private var registeredLayouts: Map<KClass<out MainLayout>, MainLayout> = mapOf(
        SongTreeLayoutController::class to songTreeLayoutController.get(),
        SongSearchLayoutController::class to songSearchLayoutController.get(),
        SongPreviewLayoutController::class to songPreviewLayoutController.get(),
        ContactLayoutController::class to contactLayoutController.get(),
        SettingsLayoutController::class to settingsLayoutController.get(),
        EditSongLayoutController::class to editSongLayoutController.get(),
        ChordsEditorLayoutController::class to chordsEditorLayoutController.get(),
        CustomSongsListLayoutController::class to customSongsListLayoutController.get(),
        FavouritesLayoutController::class to favouritesLayoutController.get(),
        PlaylistLayoutController::class to playlistLayoutController.get(),
        LatestSongsLayoutController::class to latestSongsLayoutController.get(),
        TopSongsLayoutController::class to topSongsLayoutController.get(),
        OpenHistoryLayoutController::class to openHistoryLayoutController.get(),
        MissingSongLayoutController::class to missingSongLayoutController.get(),
        PublishSongLayoutController::class to publishSongLayoutController.get(),
        AdminSongsLayoutContoller::class to adminSongsLayoutContoller.get(),
        RoomListLayoutController::class to roomListLayoutController.get(),
        RoomLobbyLayoutController::class to roomLobbyLayoutController.get(),
        BillingLayoutController::class to billingLayoutController.get(),
        WebviewLayoutController::class to webviewLayoutController.get(),
        PlaylistFillLayoutController::class to playlistFillLayoutController.get(),
    )
    private val logger = LoggerFactory.logger
    private val layoutCache = hashMapOf<Int, View>()

    suspend fun init() {
        withContext(Dispatchers.Main) {
            activity.setContentView(R.layout.main_layout)
            mainContentLayout = activity.findViewById(R.id.main_content)
            mainContentLayout.isFocusable = true
            mainContentLayout.isFocusableInTouchMode = true
            mainContentLayout.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    return@setOnKeyListener systemKeyDispatcher.onKeyDown(keyCode)
                }
                return@setOnKeyListener false
            }
            navigationMenuController.init()

            activity.supportActionBar?.hide()
        }
    }

    fun showLayout(layoutClass: KClass<out MainLayout>, disableReturn: Boolean = false): Job {
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

        return GlobalScope.launch(Dispatchers.Main) {
            showMainLayout(layoutController)
        }
    }

    private fun showMainLayout(mainLayout: MainLayout) {
        currentLayout?.onLayoutExit()
        currentLayout = mainLayout

        val transition: Transition = Fade()
        transition.duration = 200

        val (properLayoutView, _) = createLayout(mainLayout.getLayoutResourceId())

        val firstTimeView = mainContentLayout.childCount == 0

        mainContentLayout.removeAllViews()
        mainContentLayout.addView(properLayoutView)

        if (!firstTimeView) {
            TransitionManager.go(Scene(mainContentLayout, properLayoutView), transition)
        }

        mainLayout.showLayout(properLayoutView)
        postInitLayout(mainLayout)
    }

    private fun createLayout(layoutResourceId: Int): Pair<View, Boolean> {
        val inflater = activity.layoutInflater
        val properLayoutView = inflater.inflate(layoutResourceId, null)
        properLayoutView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        layoutCache[layoutResourceId] = properLayoutView
        return properLayoutView to false
    }

    private fun postInitLayout(currentLayout: MainLayout) {
        adService.updateAdBanner(currentLayout)

        if (activityController.isAndroidTv()) {
            activity.findViewById<ImageButton>(R.id.navMenuButton)?.let {
                it.isFocusableInTouchMode = true
                it.requestFocusFromTouch()
                it.isFocusableInTouchMode = false
            }
        }
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
        GlobalScope.launch(Dispatchers.Main) {
            showMainLayout(previousLayout)
        }
    }

    fun isState(compareLayoutClass: KClass<out MainLayout>): Boolean {
        return compareLayoutClass.isInstance(currentLayout)
    }

    fun onBackClicked() {
        if (navigationMenuController.isDrawerShown()) {
            navigationMenuController.navDrawerHide()
            return
        }
        safeExecute {
            currentLayout?.onBackClicked()
        }
    }

}
