package igrek.songbook.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import androidx.core.view.isVisible
import com.google.android.material.navigation.NavigationView
import igrek.songbook.R
import igrek.songbook.cast.SongCastMenuLayout
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.MainLayout
import igrek.songbook.util.waitUntil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KClass


class KickstartActivity : MainActivity() {

    private suspend fun bootstrapUI() {
        openNavItem(R.id.nav_song_cast)
        waitForLayout(SongCastMenuLayout::class)
//        appFactory.songCastMenuLayout.get().createRoom()
        safeExecute {
            appFactory.songCastMenuLayout.get().restoreRoom()
        }
    }

    private val logger: Logger = LoggerFactory.logger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalScope.launch(Dispatchers.Main) {
            appFactory.appInitializer.get().waitUntilInitialized()
            logger.debug("Kickstart: Bootstraping UI...")
            try {
                bootstrapUI()
                logger.debug("Kickstart: UI ready")
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

    private fun findButtonByText(text: String): View? {
        return getAllChildren(window.decorView).find {
            it is android.widget.Button && it.text == text
        }
    }

    private fun getAllChildren(view: View): List<View> {
        val result = ArrayList<View>()
        if (view !is ViewGroup) {
            result.add(view)
        } else {
            for (index in 0 until view.childCount) {
                val child = view.getChildAt(index)
                result.addAll(getAllChildren(child))
            }
        }
        return result
    }

    private fun MutableInteractionSource.simulateClick() {
        val interaction = this
        GlobalScope.launch {
            delay(1000)
            val press = PressInteraction.Press(Offset.Zero.copy(100f, 20f))
            interaction.emit(press)
            delay(1000)
            interaction.emit(PressInteraction.Release(press))
        }
    }

    private suspend fun waitForLayout(laoutClass: KClass<out MainLayout>) {
        var interval = 100L
        while (appFactory.layoutController.get().initializedLayout != laoutClass) {
            logger.debug("waiting for layout ${laoutClass.simpleName}")
            delay(interval)
            interval *= 2
        }
    }

}

