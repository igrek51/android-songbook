package igrek.songbook.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.core.view.isVisible
import com.google.android.material.navigation.NavigationView
import igrek.songbook.R
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.appFactory
import igrek.songbook.util.waitUntil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class KickstartActivity : MainActivity() {

    private suspend fun bootstrapUI() {
        openNavItem(R.id.nav_song_cast)
//        clickButtonById(R.id.createNewRoomButton)
    }

    private val logger: Logger = LoggerFactory.logger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalScope.launch(Dispatchers.Main) {
            appFactory.appInitializer.get().waitUntilInitialized()
            logger.debug("WIP: Bootstraping UI...")
            try {
                bootstrapUI()
                logger.debug("WIP: UI ready")
            } catch (e: Throwable) {
                logger.error("Error during bootstraping UI", e)
            }
        }
    }

    private suspend fun openNavItem(navItemResId: Int) {
        findViewById<ImageButton>(R.id.navMenuButton).performClick()
        waitUntil {
            findViewById<NavigationView>(R.id.nav_view)?.isVisible == true
        }
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menuItem: MenuItem? = navigationView?.menu?.findItem(navItemResId)
        requireNotNull(menuItem)
        navigationView.menu.performIdentifierAction(menuItem.itemId, 0)
    }

    private suspend fun clickButtonById(viewResId: Int) {
        waitUntil {
            findViewById<View>(viewResId)?.isVisible == true
        }
        findViewById<View>(viewResId)?.performClick()
    }

}

