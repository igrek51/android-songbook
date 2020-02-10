package igrek.songbook.layout.navigation

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.about.AboutLayoutController
import igrek.songbook.about.HelpLayoutController
import igrek.songbook.activity.ActivityController
import igrek.songbook.admin.antechamber.AdminSongsLayoutContoller
import igrek.songbook.contact.ContactLayoutController
import igrek.songbook.contact.SendMessageService
import igrek.songbook.custom.CustomSongsLayoutController
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.SongsUpdater
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.settings.SettingsLayoutController
import igrek.songbook.songselection.favourite.FavouritesLayoutController
import igrek.songbook.songselection.history.OpenHistoryLayoutController
import igrek.songbook.songselection.latest.LatestSongsLayoutController
import igrek.songbook.songselection.random.RandomSongOpener
import igrek.songbook.songselection.search.SongSearchLayoutController
import igrek.songbook.songselection.tree.SongTreeLayoutController
import igrek.songbook.system.SoftKeyboardService
import java.util.*
import javax.inject.Inject

class NavigationMenuController {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var uiInfoService: Lazy<UiInfoService>
    @Inject
    lateinit var uiResourceService: Lazy<UiResourceService>
    @Inject
    lateinit var activityController: Lazy<ActivityController>
    @Inject
    lateinit var helpLayoutController: Lazy<HelpLayoutController>
    @Inject
    lateinit var aboutLayoutController: Lazy<AboutLayoutController>
    @Inject
    lateinit var layoutController: Lazy<LayoutController>
    @Inject
    lateinit var songsUpdater: Lazy<SongsUpdater>
    @Inject
    lateinit var softKeyboardService: Lazy<SoftKeyboardService>
    @Inject
    lateinit var randomSongOpener: Lazy<RandomSongOpener>
    @Inject
    lateinit var sendMessageService: Lazy<SendMessageService>

    private var drawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private val actionsMap = HashMap<Int, () -> Unit>()
    private val logger = LoggerFactory.logger

    init {
        DaggerIoc.factoryComponent.inject(this)
        initOptionActionsMap()
    }

    private fun initOptionActionsMap() {
        actionsMap[R.id.nav_songs_list] = { layoutController.get().showLayout(SongTreeLayoutController::class) }
        actionsMap[R.id.nav_search] = { layoutController.get().showLayout(SongSearchLayoutController::class) }
        actionsMap[R.id.nav_favourites] = { layoutController.get().showLayout(FavouritesLayoutController::class) }
        actionsMap[R.id.nav_playlists] = { layoutController.get().showLayout(PlaylistLayoutController::class) }
        actionsMap[R.id.nav_update_db] = { songsUpdater.get().updateSongsDb() }
        actionsMap[R.id.nav_custom_songs] = { layoutController.get().showLayout(CustomSongsLayoutController::class) }
        actionsMap[R.id.nav_random_song] = { randomSongOpener.get().openRandomSong() }
        actionsMap[R.id.nav_settings] = { layoutController.get().showLayout(SettingsLayoutController::class) }
        actionsMap[R.id.nav_help] = { helpLayoutController.get().showUIHelp() }
        actionsMap[R.id.nav_about] = { aboutLayoutController.get().showAbout() }
        actionsMap[R.id.nav_exit] = { activityController.get().quit() }
        actionsMap[R.id.nav_contact] = { layoutController.get().showLayout(ContactLayoutController::class) }
        actionsMap[R.id.nav_missing_song] = { sendMessageService.get().requestMissingSong() }
        actionsMap[R.id.nav_history] = { layoutController.get().showLayout(OpenHistoryLayoutController::class) }
        actionsMap[R.id.nav_latest] = { layoutController.get().showLayout(LatestSongsLayoutController::class) }
        actionsMap[R.id.nav_admin_antechamber] = { layoutController.get().showLayout(AdminSongsLayoutContoller::class) }
    }

    fun init() {
        drawerLayout = activity.findViewById(R.id.drawer_layout)
        navigationView = activity.findViewById(R.id.nav_view)

        navigationView!!.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            drawerLayout!!.closeDrawers()
            val id = menuItem.itemId
            if (actionsMap.containsKey(id)) {
                val action = actionsMap[id]
                // postpone action - smoother navigation hide
                Handler(Looper.getMainLooper()).post {
                    SafeExecutor().execute(action!!)
                }
            } else {
                logger.warn("unknown navigation item has been selected.")
            }
            Handler().postDelayed({
                // unhighlight all menu items
                if (navigationView != null) {
                    for (id1 in 0 until navigationView!!.menu.size())
                        navigationView!!.menu.getItem(id1).isChecked = false
                }
            }, 500)
            true
        }
    }

    fun setAdminMenu() {
        navigationView?.let {
            it.menu.clear()
            it.inflateMenu(R.menu.menu_nav_admin)
        }
    }

    fun navDrawerShow() {
        drawerLayout!!.openDrawer(GravityCompat.START)
        softKeyboardService.get().hideSoftKeyboard()
    }

    fun navDrawerHide() {
        drawerLayout?.closeDrawers()
    }

    fun isDrawerShown(): Boolean {
        return drawerLayout!!.isDrawerOpen(GravityCompat.START)
    }

}
