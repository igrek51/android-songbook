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
import igrek.songbook.cast.SongCastLobbyLayout
import igrek.songbook.cast.SongCastMenuLayout
import igrek.songbook.custom.CustomSongsListLayoutController
import igrek.songbook.editor.SongEditorLayoutController
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.info.logview.LogsLayoutController
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.SingletonInject
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass


class LayoutController {
    private val activity: AppCompatActivity by LazyExtractor(appFactory.appCompatActivity)
    private val navigationMenuController: NavigationMenuController by LazyExtractor(appFactory.navigationMenuController)
    private val activityController: ActivityController by LazyExtractor(appFactory.activityController)
    private val adService: AdService by LazyExtractor(appFactory.adService)
    private val systemKeyDispatcher: SystemKeyDispatcher by LazyExtractor(appFactory.systemKeyDispatcher)

    private var mainContentLayout: CoordinatorLayout? = null
    private var currentLayout: MainLayout? = null
    private var layoutHistory: MutableList<MainLayout> = mutableListOf()
    private var registeredLayoutInjectors: Map<KClass<out MainLayout>, SingletonInject<out MainLayout>> = mapOf(
        SongTreeLayoutController::class to appFactory.songTreeLayoutController,
        SongSearchLayoutController::class to appFactory.songSearchLayoutController,
        SongPreviewLayoutController::class to appFactory.songPreviewLayoutController,
        ContactLayoutController::class to appFactory.contactLayoutController,
        SettingsLayoutController::class to appFactory.settingsLayoutController,
        SongEditorLayoutController::class to appFactory.songEditorLayoutController,
        CustomSongsListLayoutController::class to appFactory.customSongsListLayoutController,
        FavouritesLayoutController::class to appFactory.favouritesLayoutController,
        PlaylistLayoutController::class to appFactory.playlistLayoutController,
        LatestSongsLayoutController::class to appFactory.latestSongsLayoutController,
        TopSongsLayoutController::class to appFactory.topSongsLayoutController,
        OpenHistoryLayoutController::class to appFactory.openHistoryLayoutController,
        MissingSongLayoutController::class to appFactory.missingSongLayoutController,
        PublishSongLayoutController::class to appFactory.publishSongLayoutController,
        AdminSongsLayoutContoller::class to appFactory.adminSongsLayoutContoller,
        RoomListLayoutController::class to appFactory.roomListLayoutController,
        RoomLobbyLayoutController::class to appFactory.roomLobbyLayoutController,
        BillingLayoutController::class to appFactory.billingLayoutController,
        WebviewLayoutController::class to appFactory.webviewLayoutController,
        PlaylistFillLayoutController::class to appFactory.playlistFillLayoutController,
        LogsLayoutController::class to appFactory.logsLayoutController,
        SongCastLobbyLayout::class to appFactory.songCastLobbyLayout,
        SongCastMenuLayout::class to appFactory.songCastMenuLayout,
    )
    private val logger = LoggerFactory.logger
    private val layoutCache = hashMapOf<Int, View>()
    var initializedLayout: KClass<out MainLayout>? = null
        private set

    suspend fun init() {
        withContext(Dispatchers.Main) {
            activity.setContentView(R.layout.main_layout)
            mainContentLayout = activity.findViewById<CoordinatorLayout>(R.id.main_content).also {
                it.isFocusable = true
                it.isFocusableInTouchMode = true
                it.setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        return@setOnKeyListener systemKeyDispatcher.onKeyDown(keyCode)
                    }
                    return@setOnKeyListener false
                }
            }
            navigationMenuController.init()

            activity.supportActionBar?.hide()
        }
    }

    fun showLayout(
        layoutClass: KClass<out MainLayout>,
        disableReturn: Boolean = false,
        onShown: (() -> Unit) = {},
    ): Job {
        val layoutInjector = registeredLayoutInjectors[layoutClass]
            ?: throw IllegalArgumentException("${layoutClass.simpleName} class not registered as layout")
        val layoutController: MainLayout = layoutInjector.get()

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
            onShown()
        }
    }

    private fun showMainLayout(mainLayout: MainLayout) {
        currentLayout?.onLayoutExit()
        currentLayout = mainLayout

        val transition: Transition = Fade()
        transition.duration = 200

        val (properLayoutView, _) = createLayout(mainLayout.getLayoutResourceId())

        val mainContentLayoutN: CoordinatorLayout = mainContentLayout ?: return
        val firstTimeView = mainContentLayoutN.childCount == 0

        mainContentLayoutN.removeAllViews()
        mainContentLayoutN.addView(properLayoutView)

        if (!firstTimeView) {
            TransitionManager.go(Scene(mainContentLayoutN, properLayoutView), transition)
        }

        mainLayout.showLayout(properLayoutView)
        postInitLayout(mainLayout)
        initializedLayout = mainLayout::class
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
            logger.error(e)
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
