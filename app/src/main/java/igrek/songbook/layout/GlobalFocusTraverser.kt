package igrek.songbook.layout

import android.app.Activity
import android.view.View
import androidx.core.view.isVisible
import igrek.songbook.R
import igrek.songbook.custom.CustomSongsListLayoutController
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songselection.top.TopSongsLayoutController

class GlobalFocusTraverser(
    activity: LazyInject<Activity> = appFactory.activity,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
    songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) {
    private val activity by LazyExtractor(activity)
    private val layoutController by LazyExtractor(layoutController)
    private val navigationMenuController by LazyExtractor(navigationMenuController)
    private val songPreviewLayoutController by LazyExtractor(songPreviewLayoutController)
    private val uiInfoService by LazyExtractor(uiInfoService)

    private val debugMode: Boolean = true

    fun moveToNextView(nextViewProvider: (Int) -> Int): Boolean {
        val currentFocusId = activity.currentFocus?.id ?: 0

        if (debugMode && currentFocusId > 0) {
            val viewName = activity.currentFocus?.javaClass?.simpleName
            val resourceName = activity.resources.getResourceName(currentFocusId)
            logger.debug("Current focus view: $resourceName - $viewName")
        }

        val nextViewId = nextViewProvider(currentFocusId)
        if (nextViewId != 0 && nextViewId != currentFocusId) {
            activity.findViewById<View>(nextViewId)?.let {
                val result = it.requestFocusFromTouch()
                if (debugMode && nextViewId > 0) {
                    val nextViewClass = it.javaClass.simpleName
                    val nextResourceName = activity.resources.getResourceName(nextViewId)
                    when (result) {
                        true -> logger.debug("focus set to $nextResourceName - $nextViewClass")
                        false -> logger.warn("requesting focus failed for $nextResourceName - $nextViewClass")
                    }
                }
                return result
            }
        }
        return false
    }

    fun nextRightView(currentViewId: Int): Int {
        if (navigationMenuController.isDrawerShown()) {
            navigationMenuController.navDrawerHide()
            return R.id.navMenuButton
        }

        return when {
            layoutController.isState(SongPreviewLayoutController::class) -> when (currentViewId) {
                R.id.main_content -> 0
                R.id.navMenuButton -> R.id.songInfoButton
                R.id.songInfoButton -> R.id.chordsHelpButton
                R.id.chordsHelpButton -> R.id.setFavouriteButton
                R.id.setFavouriteButton -> R.id.transposeButton
                R.id.transposeButton -> R.id.autoscrollButton
                R.id.autoscrollButton -> when {
                    activity.findViewById<View>(R.id.screenShareButton)?.isVisible == true -> R.id.screenShareButton
                    else -> R.id.moreActionsButton
                }
                R.id.screenShareButton -> R.id.moreActionsButton
                R.id.autoscrollToggleButton -> R.id.speedPlusButton
                R.id.speedMinusButton -> R.id.speedSeekbar
                R.id.speedSeekbar -> R.id.speedPlusButton
                R.id.transposeM5Button -> R.id.transposeM1Button
                R.id.transposeM1Button -> R.id.transpose0Button
                R.id.transpose0Button -> R.id.transposeP1Button
                R.id.transposeP1Button -> R.id.transposeP5Button
                R.id.transposeP5Button -> R.id.transposeP5Button
                else -> 0
            }
            currentViewId == R.id.main_content -> R.id.navMenuButton
            currentViewId == R.id.navMenuButton -> when {
                activity.findViewById<View>(R.id.goBackButton)?.isVisible == true -> R.id.goBackButton
                activity.findViewById<View>(R.id.languageFilterButton)?.isVisible == true -> R.id.languageFilterButton
                activity.findViewById<View>(R.id.searchSongButton)?.isVisible == true -> R.id.searchSongButton
                else -> 0
            }
            layoutController.isState(CustomSongsListLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> when {
                    activity.findViewById<View>(R.id.goBackButton)?.isVisible == true -> R.id.goBackButton
                    activity.findViewById<View>(R.id.languageFilterButton)?.isVisible == true -> R.id.languageFilterButton
                    else -> 0
                }
                R.id.goBackButton -> R.id.languageFilterButton
                R.id.languageFilterButton -> R.id.addCustomSongButton
                R.id.addCustomSongButton -> R.id.moreActionsButton
                else -> 0
            }
            layoutController.isState(PlaylistLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> when {
                    activity.findViewById<View>(R.id.goBackButton)?.isVisible == true -> R.id.goBackButton
                    activity.findViewById<View>(R.id.addPlaylistButton)?.isVisible == true -> R.id.addPlaylistButton
                    else -> 0
                }
                else -> 0
            }
            currentViewId == R.id.goBackButton -> when {
                activity.findViewById<View>(R.id.languageFilterButton)?.isVisible == true -> R.id.languageFilterButton
                activity.findViewById<View>(R.id.searchSongButton)?.isVisible == true -> R.id.searchSongButton
                else -> 0
            }
            currentViewId == R.id.languageFilterButton -> R.id.searchSongButton
            currentViewId == R.id.searchSongButton -> 0
            else -> 0
        }
    }

    fun nextLeftView(currentViewId: Int): Int {
        if (navigationMenuController.isDrawerShown()) {
            when (currentViewId) {
                R.id.navMenuButton, R.id.itemsList, R.id.itemsListView, R.id.main_content -> return R.id.nav_view
            }
        }

        return when {
            layoutController.isState(SongPreviewLayoutController::class) -> when (currentViewId) {
                R.id.main_content -> 0
                R.id.navMenuButton -> R.id.navMenuButton
                R.id.moreActionsButton -> when {
                    activity.findViewById<View>(R.id.screenShareButton)?.isVisible == true -> R.id.screenShareButton
                    else -> R.id.autoscrollButton
                }
                R.id.screenShareButton -> R.id.autoscrollButton
                R.id.autoscrollButton -> R.id.transposeButton
                R.id.transposeButton -> R.id.setFavouriteButton
                R.id.setFavouriteButton -> R.id.chordsHelpButton
                R.id.chordsHelpButton -> R.id.songInfoButton
                R.id.songInfoButton -> R.id.navMenuButton
                R.id.autoscrollToggleButton -> R.id.speedMinusButton
                R.id.speedPlusButton -> R.id.speedSeekbar
                R.id.speedSeekbar -> R.id.speedMinusButton
                R.id.transposeP5Button -> R.id.transposeP1Button
                R.id.transposeP1Button -> R.id.transpose0Button
                R.id.transpose0Button -> R.id.transposeM1Button
                R.id.transposeM1Button -> R.id.transposeM5Button
                R.id.transposeM5Button -> R.id.transposeM5Button
                else -> R.id.navMenuButton
            }
            currentViewId == R.id.main_content -> R.id.navMenuButton
            currentViewId == R.id.navMenuButton -> R.id.navMenuButton
            layoutController.isState(CustomSongsListLayoutController::class) -> when (currentViewId) {
                R.id.moreActionsButton -> R.id.addCustomSongButton
                R.id.addCustomSongButton -> R.id.languageFilterButton
                R.id.languageFilterButton -> when {
                    activity.findViewById<View>(R.id.goBackButton)?.isVisible == true -> R.id.goBackButton
                    else -> R.id.navMenuButton
                }
                R.id.goBackButton -> R.id.navMenuButton
                else -> R.id.navMenuButton
            }
            layoutController.isState(PlaylistLayoutController::class) -> when (currentViewId) {
                R.id.goBackButton -> R.id.navMenuButton
                R.id.addPlaylistButton -> R.id.navMenuButton
                else -> R.id.navMenuButton
            }
            currentViewId == R.id.languageFilterButton -> when {
                activity.findViewById<View>(R.id.goBackButton)?.isVisible == true -> R.id.goBackButton
                else -> R.id.navMenuButton
            }
            currentViewId == R.id.searchSongButton -> when {
                activity.findViewById<View>(R.id.languageFilterButton)?.isVisible == true -> R.id.languageFilterButton
                activity.findViewById<View>(R.id.goBackButton)?.isVisible == true -> R.id.goBackButton
                else -> R.id.navMenuButton
            }
            currentViewId == R.id.searchFilterClearButton -> R.id.searchFilterEdit
            currentViewId == R.id.searchFilterEdit -> R.id.navMenuButton
            else -> R.id.navMenuButton
        }
    }

    fun nextDownView(currentViewId: Int): Int {
        if (navigationMenuController.isDrawerShown()) {
            when (currentViewId) {
                R.id.navMenuButton, R.id.itemsList, R.id.itemsListView, R.id.main_content -> return R.id.nav_view
            }
        }
        if (uiInfoService.isSnackbarShown()) {
            if (uiInfoService.focusSnackBar())
                return -1
        }

        return when {
            layoutController.isState(SongPreviewLayoutController::class) -> when (currentViewId) {
                R.id.main_content -> when {
                    songPreviewLayoutController.canScrollDown() -> 0
                    else -> R.id.navMenuButton
                }
                R.id.navMenuButton, R.id.songInfoButton, R.id.chordsHelpButton, R.id.setFavouriteButton,
                R.id.transposeButton, R.id.autoscrollButton, R.id.screenShareButton, R.id.moreActionsButton -> when {
                    activity.findViewById<View>(R.id.transpose0Button)?.isVisible == true && songPreviewLayoutController.isTransposePanelVisible() -> R.id.transpose0Button
                    activity.findViewById<View>(R.id.autoscrollToggleButton)?.isVisible == true && songPreviewLayoutController.isAutoscrollPanelVisible() -> R.id.autoscrollToggleButton
                    else -> R.id.main_content
                }
                R.id.autoscrollToggleButton -> R.id.speedSeekbar
                else -> 0
            }
            currentViewId == R.id.main_content -> R.id.navMenuButton
            currentViewId == R.id.navMenuButton -> when {
                layoutController.isState(TopSongsLayoutController::class) -> R.id.itemsList
                layoutController.isState(CustomSongsListLayoutController::class) -> R.id.itemsListView
                layoutController.isState(PlaylistLayoutController::class) -> R.id.playlistListView
                else -> 0
            }
            else -> 0
        }
    }

    fun nextUpView(currentViewId: Int): Int {
        if (navigationMenuController.isDrawerShown()) {
            when (currentViewId) {
                R.id.navMenuButton, R.id.itemsList, R.id.itemsListView, R.id.main_content -> return R.id.nav_view
            }
        }

        return when {
            layoutController.isState(SongPreviewLayoutController::class) -> when (currentViewId) {
                R.id.main_content -> when {
                    songPreviewLayoutController.canScrollUp() -> 0
                    else -> R.id.navMenuButton
                }
                R.id.navMenuButton -> R.id.navMenuButton
                R.id.songInfoButton, R.id.chordsHelpButton, R.id.setFavouriteButton,
                R.id.transposeButton, R.id.autoscrollButton, R.id.screenShareButton, R.id.moreActionsButton -> R.id.navMenuButton
                R.id.transposeM5Button, R.id.transposeM1Button, R.id.transpose0Button,
                R.id.transposeP1Button, R.id.transposeP5Button -> R.id.transposeButton
                R.id.autoscrollToggleButton -> R.id.autoscrollButton
                R.id.speedMinusButton, R.id.speedSeekbar, R.id.speedPlusButton -> R.id.autoscrollToggleButton
                else -> 0
            }
            currentViewId == R.id.main_content -> R.id.navMenuButton
            currentViewId == R.id.navMenuButton -> R.id.navMenuButton
            else -> 0
        }
    }
}