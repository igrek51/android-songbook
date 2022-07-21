package igrek.songbook.layout

import android.app.Activity
import android.view.View
import igrek.songbook.R
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.songselection.top.TopSongsLayoutController

class NextFocusTraverser(
    activity: LazyInject<Activity> = appFactory.activity,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
) {
    private val activity by LazyExtractor(activity)
    private val layoutController by LazyExtractor(layoutController)
    private val navigationMenuController by LazyExtractor(navigationMenuController)

    fun moveToNextView(nextViewProvider: (Int) -> Int): Boolean {
        val currentFocusId = activity.currentFocus?.id ?: 0

        if (currentFocusId != 0) {
            val viewName = activity.currentFocus?.javaClass?.simpleName
            val resourceName = activity.resources.getResourceName(currentFocusId)
            logger.debug("Global Traverser: current focus view: $currentFocusId - $viewName - $resourceName")
        }

        val nextViewId = nextViewProvider(currentFocusId)
        if (nextViewId != 0 && nextViewId != currentFocusId) {
            activity.findViewById<View>(nextViewId)?.run {
                requestFocus()
                return true
            }
        }
        return false
    }

    fun nextRightView(currentViewId: Int): Int {
        if (navigationMenuController.isDrawerShown()) {
            navigationMenuController.navDrawerHide()
            return R.id.navMenuButton
        }

        return when (currentViewId) {
            R.id.main_content -> R.id.navMenuButton
            R.id.navMenuButton -> R.id.languageFilterButton
            R.id.languageFilterButton -> R.id.searchSongButton
            R.id.searchSongButton -> 0
            else -> 0
        }
    }

    fun nextLeftView(currentViewId: Int): Int {
        if (navigationMenuController.isDrawerShown()) {
            if (currentViewId == R.id.navMenuButton)
                return R.id.nav_view
        }

        return when (currentViewId) {
            R.id.main_content -> R.id.navMenuButton
            R.id.navMenuButton -> 0
            R.id.languageFilterButton -> R.id.navMenuButton
            R.id.searchSongButton -> R.id.languageFilterButton
            else -> 0
        }
    }

    fun nextDownView(currentViewId: Int): Int {
        if (navigationMenuController.isDrawerShown()) {
            if (currentViewId == R.id.navMenuButton)
                return R.id.nav_view
        }

        return when (currentViewId) {
            R.id.main_content -> R.id.navMenuButton
            R.id.navMenuButton -> when {
                layoutController.isState(TopSongsLayoutController::class) -> R.id.itemsList
                else -> 0
            }
            else -> 0
        }
    }

    fun nextUpView(currentViewId: Int): Int {
        return when (currentViewId) {
            R.id.main_content -> R.id.navMenuButton
            R.id.navMenuButton -> 0
            else -> 0
        }
    }
}