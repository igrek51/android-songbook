package igrek.songbook.service.navmenu

import android.app.Activity
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.logger.LoggerFactory
import igrek.songbook.service.activity.ActivityController
import igrek.songbook.service.errorcheck.SafeExecutor
import igrek.songbook.service.info.UiInfoService
import igrek.songbook.service.layout.LayoutController
import igrek.songbook.service.layout.about.AboutLayoutController
import igrek.songbook.service.layout.contact.ContactLayoutController
import igrek.songbook.service.layout.help.HelpLayoutController
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
    lateinit var activityController: dagger.Lazy<ActivityController>
    @Inject
    lateinit var contactLayoutController: dagger.Lazy<ContactLayoutController>
    @Inject
    lateinit var helpLayoutController: dagger.Lazy<HelpLayoutController>
    @Inject
    lateinit var aboutLayoutController: dagger.Lazy<AboutLayoutController>
    @Inject
    lateinit var layoutController: dagger.Lazy<LayoutController>

    init {
        DaggerIoc.getFactoryComponent().inject(this)
        initOptionActionsMap()
    }

    private fun initOptionActionsMap() {
        actionsMap[R.id.nav_songs_list] = Runnable { layoutController.get().showSongTree() }
        actionsMap[R.id.nav_search] = Runnable { layoutController.get().showSongSearch() }
        actionsMap[R.id.nav_update_db] = Runnable { uiInfoService.showToast("Songs database is up-to-date") }
        actionsMap[R.id.nav_import_song] = Runnable { uiInfoService.showToast("not implemented yet") }
        actionsMap[R.id.nav_settings] = Runnable { uiInfoService.showToast("not implemented yet") }
        actionsMap[R.id.nav_help] = Runnable { helpLayoutController.get().showUIHelp() }
        actionsMap[R.id.nav_about] = Runnable { aboutLayoutController.get().showAbout() }
        actionsMap[R.id.nav_exit] = Runnable { activityController.get().quit() }
        actionsMap[R.id.nav_contact] = Runnable { contactLayoutController.get().showContact() }
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
                SafeExecutor().execute(action)
            } else {
                logger.warn("unknown navigation item has been selected.")
            }
            true
        }
    }

    fun navDrawerShow() {
        drawerLayout!!.openDrawer(GravityCompat.START)
        // unhighlight all menu items
        for (id in 0 until navigationView!!.menu.size())
            navigationView!!.menu.getItem(id).isChecked = false
    }

}
