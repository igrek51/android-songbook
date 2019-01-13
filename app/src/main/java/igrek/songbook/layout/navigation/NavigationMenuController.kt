package igrek.songbook.layout.navigation

import android.app.Activity
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import igrek.songbook.R
import igrek.songbook.about.AboutLayoutController
import igrek.songbook.about.HelpLayoutController
import igrek.songbook.activity.ActivityController
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.SongsRepository
import igrek.songbook.songedit.SongEditService
import igrek.songbook.songselection.RandomSongOpener
import igrek.songbook.system.SoftKeyboardService
import java.util.*
import javax.inject.Inject

class NavigationMenuController {

    private var drawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private val actionsMap = HashMap<Int, Runnable>()
    private val logger = LoggerFactory.getLogger()

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var activityController: dagger.Lazy<ActivityController>
    @Inject
    lateinit var helpLayoutController: dagger.Lazy<HelpLayoutController>
    @Inject
    lateinit var aboutLayoutController: dagger.Lazy<AboutLayoutController>
    @Inject
    lateinit var layoutController: dagger.Lazy<LayoutController>
    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var softKeyboardService: SoftKeyboardService
    @Inject
    lateinit var songEditService: SongEditService
    @Inject
    lateinit var randomSongOpener: RandomSongOpener

    init {
        DaggerIoc.getFactoryComponent().inject(this)
        initOptionActionsMap()
    }

    private fun initOptionActionsMap() {
        actionsMap[R.id.nav_songs_list] = Runnable { layoutController.get().showSongTree() }
        actionsMap[R.id.nav_search] = Runnable { layoutController.get().showSongSearch() }
        actionsMap[R.id.nav_favourites] = Runnable { layoutController.get().showFavourites() }
        actionsMap[R.id.nav_update_db] = Runnable { songsRepository.updateSongsDb() }
        actionsMap[R.id.nav_custom_songs] = Runnable { songEditService.showAddSongScreen() }
        actionsMap[R.id.nav_random_song] = Runnable { randomSongOpener.openRandomSong() }
        actionsMap[R.id.nav_settings] = Runnable { layoutController.get().showSettings() }
        actionsMap[R.id.nav_help] = Runnable { helpLayoutController.get().showUIHelp() }
        actionsMap[R.id.nav_about] = Runnable { aboutLayoutController.get().showAbout() }
        actionsMap[R.id.nav_exit] = Runnable { activityController.get().quit() }
        actionsMap[R.id.nav_contact] = Runnable { layoutController.get().showContact() }
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
                Handler().post {
                    SafeExecutor().execute(action)
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

    fun navDrawerShow() {
        drawerLayout!!.openDrawer(GravityCompat.START)
        softKeyboardService.hideSoftKeyboard()
    }

    fun navDrawerHide() {
        drawerLayout?.closeDrawers()
    }

    fun isDrawerShown(): Boolean {
        return drawerLayout!!.isDrawerOpen(GravityCompat.START)
    }

}
