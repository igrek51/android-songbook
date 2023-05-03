package igrek.songbook.layout.navigation

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import igrek.songbook.R
import igrek.songbook.about.AboutLayoutController
import igrek.songbook.activity.ActivityController
import igrek.songbook.admin.antechamber.AdminSongsLayoutContoller
import igrek.songbook.billing.BillingLayoutController
import igrek.songbook.cast.SongCastLayout
import igrek.songbook.chords.diagram.ChordDiagramsService
import igrek.songbook.custom.CustomSongService
import igrek.songbook.custom.CustomSongsListLayoutController
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.SongsUpdater
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.send.ContactLayoutController
import igrek.songbook.settings.SettingsLayoutController
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.favourite.FavouritesLayoutController
import igrek.songbook.songselection.history.OpenHistoryLayoutController
import igrek.songbook.songselection.latest.LatestSongsLayoutController
import igrek.songbook.songselection.random.RandomSongOpener
import igrek.songbook.songselection.search.SongSearchLayoutController
import igrek.songbook.songselection.top.TopSongsLayoutController
import igrek.songbook.songselection.tree.SongTreeLayoutController
import igrek.songbook.system.SoftKeyboardService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class NavigationMenuController(
    activity: LazyInject<Activity> = appFactory.activity,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    aboutLayoutController: LazyInject<AboutLayoutController> = appFactory.aboutLayoutController,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    songsUpdater: LazyInject<SongsUpdater> = appFactory.songsUpdater,
    softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
    randomSongOpener: LazyInject<RandomSongOpener> = appFactory.randomSongOpener,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
    chordDiagramsService: LazyInject<ChordDiagramsService> = appFactory.chordDiagramsService,
    customSongService: LazyInject<CustomSongService> = appFactory.customSongService,
) {
    private val activity by LazyExtractor(activity)
    private val activityController by LazyExtractor(activityController)
    private val aboutLayoutController by LazyExtractor(aboutLayoutController)
    private val layoutController by LazyExtractor(layoutController)
    private val songsUpdater by LazyExtractor(songsUpdater)
    private val softKeyboardService by LazyExtractor(softKeyboardService)
    private val randomSongOpener by LazyExtractor(randomSongOpener)
    private val songOpener by LazyExtractor(songOpener)
    private val chordsDiagramsService by LazyExtractor(chordDiagramsService)
    private val customSongService by LazyExtractor(customSongService)

    private var drawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private val actionsMap = HashMap<Int, () -> Unit>()
    private val logger = LoggerFactory.logger

    init {
        initOptionActionsMap()
    }

    private fun initOptionActionsMap() {
        actionsMap[R.id.nav_songs_list] =
            { layoutController.showLayout(SongTreeLayoutController::class) }
        actionsMap[R.id.nav_search] =
            { layoutController.showLayout(SongSearchLayoutController::class) }
        actionsMap[R.id.nav_favourites] =
            { layoutController.showLayout(FavouritesLayoutController::class) }
        actionsMap[R.id.nav_playlists] =
            { layoutController.showLayout(PlaylistLayoutController::class) }
        actionsMap[R.id.nav_update_db] =
            { songsUpdater.updateSongsDbAsync(forced = true) }
        actionsMap[R.id.nav_custom_songs] =
            { layoutController.showLayout(CustomSongsListLayoutController::class) }
        actionsMap[R.id.nav_random_song] =
            { randomSongOpener.openRandomSong() }
        actionsMap[R.id.nav_settings] =
            { layoutController.showLayout(SettingsLayoutController::class) }
        actionsMap[R.id.nav_about] =
            { aboutLayoutController.showAbout() }
        actionsMap[R.id.nav_exit] =
            { activityController.quit() }
        actionsMap[R.id.nav_contact] =
            { layoutController.showLayout(ContactLayoutController::class) }
//        actionsMap[R.id.nav_missing_song] =
//            { sendMessageService.requestMissingSong() }
        actionsMap[R.id.nav_history] =
            { layoutController.showLayout(OpenHistoryLayoutController::class) }
        actionsMap[R.id.nav_latest] =
            { layoutController.showLayout(LatestSongsLayoutController::class) }
        actionsMap[R.id.nav_top_songs] =
            { layoutController.showLayout(TopSongsLayoutController::class) }
        actionsMap[R.id.nav_last_song] =
            { songOpener.openLastSong() }
        actionsMap[R.id.nav_admin_antechamber] =
            { layoutController.showLayout(AdminSongsLayoutContoller::class) }
        actionsMap[R.id.nav_chord_diagram] =
            { chordsDiagramsService.showFindChordByNameMenu() }
        actionsMap[R.id.nav_song_cast] =
            { layoutController.showLayout(SongCastLayout::class) }
        actionsMap[R.id.nav_purchase] =
            { layoutController.showLayout(BillingLayoutController::class) }
    }

    fun init() {
        drawerLayout = activity.findViewById(R.id.drawer_layout)
        navigationView = activity.findViewById(R.id.nav_view)

        bindCustomButtonActions()

        navigationView?.setNavigationItemSelectedListener { menuItem ->
            GlobalScope.launch(Dispatchers.Main) {
                // set item as selected to persist highlight
                menuItem.isChecked = true
                drawerLayout?.closeDrawers()
                val id = menuItem.itemId
                if (actionsMap.containsKey(id)) {
                    val action = actionsMap[id]
                    // postpone action - smoother navigation hide
                    Handler(Looper.getMainLooper()).post {
                        safeExecute {
                            action?.invoke()
                        }
                    }
                } else {
                    logger.warn("unknown navigation item has been selected.")
                }
                unhighlightMenuItems()
            }
            true
        }
    }

    private fun bindCustomButtonActions() {
        var menuItem: MenuItem? = navigationView?.menu?.findItem(R.id.nav_custom_songs)
        val navAddCustomSongButton =
            menuItem?.actionView?.findViewById<ImageButton>(R.id.navAddCustomSongButton)
        navAddCustomSongButton?.setOnClickListener {
            closeDrawerAndCallAction {
                customSongService.showAddSongScreen()
            }
        }
        if (navAddCustomSongButton == null)
            logger.error("Navigation button not found: navAddCustomSongButton")

        menuItem = navigationView?.menu?.findItem(R.id.nav_about)
        val navHelpExtraButton = menuItem?.actionView?.findViewById<ImageButton>(R.id.navHelpExtraButton)
        navHelpExtraButton?.setOnClickListener {
            closeDrawerAndCallAction {
                aboutLayoutController.showManual()
            }
        }
        if (navHelpExtraButton == null)
            logger.error("Navigation button not found: navHelpExtraButton")
    }

    private fun closeDrawerAndCallAction(action: () -> Unit) {
        drawerLayout?.closeDrawers()
        // postpone action - smoother navigation hide
        Handler(Looper.getMainLooper()).post {
            safeExecute {
                action.invoke()
            }
        }
        unhighlightMenuItems()
    }

    private fun unhighlightMenuItems() {
        Handler(Looper.getMainLooper()).postDelayed({
            navigationView?.let { navigationView ->
                for (id1 in 0 until navigationView.menu.size())
                    navigationView.menu.getItem(id1).isChecked = false
            }
        }, 500)
    }

    fun hideAdminMenu() {
        GlobalScope.launch(Dispatchers.Main) {
            val menu: Menu = navigationView?.menu
                ?: run {
                    logger.error("Navigation menu not found")
                    return@launch
                }
            menu.removeItem(R.id.nav_admin_antechamber)
        }
    }

    fun navDrawerShow() {
        drawerLayout?.openDrawer(GravityCompat.START)
        softKeyboardService.hideSoftKeyboard()
        activity.findViewById<View>(R.id.nav_top_songs)?.requestFocus()
    }

    fun navDrawerHide() {
        drawerLayout?.closeDrawers()
    }

    fun isDrawerShown(): Boolean {
        return drawerLayout?.isDrawerOpen(GravityCompat.START) ?: false
    }

}
