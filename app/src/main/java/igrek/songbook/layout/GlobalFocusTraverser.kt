package igrek.songbook.layout

import android.app.Activity
import android.view.KeyEvent
import android.view.View
import androidx.core.view.isVisible
import igrek.songbook.R
import igrek.songbook.billing.BillingLayoutController
import igrek.songbook.custom.CustomSongsListLayoutController
import igrek.songbook.custom.EditSongLayoutController
import igrek.songbook.editor.ChordsEditorLayoutController
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.navigation.NavigationMenuController
import igrek.songbook.playlist.PlaylistFillLayoutController
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.playlist.PlaylistService
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

class GlobalFocusTraverser(
    activity: LazyInject<Activity> = appFactory.activity,
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    navigationMenuController: LazyInject<NavigationMenuController> = appFactory.navigationMenuController,
    songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    playlistService: LazyInject<PlaylistService> = appFactory.playlistService,
) {
    private val activity by LazyExtractor(activity)
    private val layoutController by LazyExtractor(layoutController)
    private val navigationMenuController by LazyExtractor(navigationMenuController)
    private val songPreviewLayoutController by LazyExtractor(songPreviewLayoutController)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val playlistService by LazyExtractor(playlistService)

    private val debugMode: Boolean = false

    fun moveToNextView(nextViewProvider: (Int) -> Int): Boolean {
        val currentFocusId = activity.currentFocus?.id ?: 0

        if (debugMode && currentFocusId > 0) {
            val viewName = activity.currentFocus?.javaClass?.simpleName
            val resourceName = activity.resources.getResourceName(currentFocusId)
            logger.debug("Current focus view: $resourceName - $viewName")
        }

        val nextViewId = nextViewProvider(currentFocusId)

        if (nextViewId == -1)
            return true

        if (nextViewId != 0 && nextViewId != currentFocusId) {
            activity.findViewById<View>(nextViewId)?.let {

                if (it.isClickable)
                    it.isFocusableInTouchMode = true
                val result = it.requestFocusFromTouch()
                if (it.isClickable)
                    it.isFocusableInTouchMode = false

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

    private fun handleKey(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                return moveToNextView(::nextUpView)
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                return moveToNextView(::nextDownView)
            }
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_MEDIA_REWIND,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD,
            KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD -> {
                return moveToNextView(::nextLeftView)
            }
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD,
            KeyEvent.KEYCODE_MEDIA_STEP_FORWARD -> {
                return moveToNextView(::nextRightView)
            }
        }
        return false
    }

    fun setUpDownKeyListener(view: View) {
        view.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                    if (this.handleKey(keyCode))
                        return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }

    fun nextRightView(currentViewId: Int): Int {
        if (navigationMenuController.isDrawerShown()) {
            return when (currentViewId) {
                R.id.nav_custom_songs -> {
                    R.id.navAddCustomSongButton
                }
                R.id.nav_about -> {
                    R.id.navHelpExtraButton
                }
                else -> {
                    navigationMenuController.navDrawerHide()
                    R.id.navMenuButton
                }
            }
        }

        return when {
            layoutController.isState(SongPreviewLayoutController::class) -> when {
                playlistService.goToNextOrPrevious(+1) -> -1
                else -> when (currentViewId) {
                    R.id.main_content, R.id.overlayScrollView -> R.id.navMenuButton
                    R.id.navMenuButton -> R.id.songInfoButton
                    R.id.songInfoButton -> R.id.chordsHelpButton
                    R.id.chordsHelpButton -> R.id.setFavouriteButton
                    R.id.setFavouriteButton -> R.id.transposeButton
                    R.id.transposeButton -> R.id.autoscrollButton
                    R.id.autoscrollButton -> when {
                        isViewVisible(R.id.songCastButton) -> R.id.songCastButton
                        else -> R.id.moreActionsButton
                    }
                    R.id.songCastButton -> R.id.moreActionsButton
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
            }
            currentViewId == R.id.main_content -> R.id.navMenuButton
            layoutController.isState(LatestSongsLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> R.id.updateLatestSongs
                else -> 0
            }
            layoutController.isState(CustomSongsListLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> when {
                    isViewVisible(R.id.goBackButton) -> R.id.goBackButton
                    isViewVisible(R.id.searchSongButton) -> R.id.searchSongButton
                    isViewVisible(R.id.searchFilterEdit) -> R.id.searchFilterEdit
                    else -> 0
                }
                R.id.goBackButton -> when {
                    isViewVisible(R.id.searchSongButton) -> R.id.searchSongButton
                    isViewVisible(R.id.searchFilterEdit) -> R.id.searchFilterEdit
                    else -> 0
                }
                R.id.searchSongButton -> R.id.songsSortButton
                R.id.songsSortButton -> R.id.moreActionsButton
                R.id.searchFilterEdit -> R.id.searchFilterClearButton
                R.id.searchFilterClearButton -> R.id.searchFilterClearButton
                else -> 0
            }
            layoutController.isState(EditSongLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> R.id.goBackButton
                R.id.goBackButton -> R.id.saveSongButton
                R.id.saveSongButton -> R.id.moreActionsButton
                else -> 0
            }
            layoutController.isState(ChordsEditorLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> R.id.goBackButton
                R.id.goBackButton -> R.id.tooltipEditChordsLyricsInfo
                else -> 0
            }
            layoutController.isState(PlaylistLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> when {
                    isViewVisible(R.id.goBackButton) -> R.id.goBackButton
                    isViewVisible(R.id.addPlaylistButton) -> R.id.addPlaylistButton
                    else -> 0
                }
                R.id.goBackButton -> when {
                    isViewVisible(R.id.addPlaylistButton) -> R.id.addPlaylistButton
                    else -> 0
                }
                else -> 0
            }
            currentViewId == R.id.navMenuButton -> when {
                isViewVisible(R.id.goBackButton) -> R.id.goBackButton
                isViewVisible(R.id.languageFilterButton) -> R.id.languageFilterButton
                isViewVisible(R.id.searchSongButton) -> R.id.searchSongButton
                else -> 0
            }
            currentViewId == R.id.goBackButton -> when {
                isViewVisible(R.id.languageFilterButton) -> R.id.languageFilterButton
                isViewVisible(R.id.searchSongButton) -> R.id.searchSongButton
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
                R.id.nav_top_songs -> return R.id.nav_top_songs
                R.id.navAddCustomSongButton -> return R.id.nav_custom_songs
                R.id.navHelpExtraButton -> return R.id.nav_about
            }
        }

        return when {
            layoutController.isState(SongPreviewLayoutController::class) -> when {
                playlistService.goToNextOrPrevious(-1) -> -1
                else -> when (currentViewId) {
                    R.id.main_content, R.id.overlayScrollView -> R.id.navMenuButton
                    R.id.navMenuButton -> {
                        navigationMenuController.navDrawerShow()
                        -1
                    }
                    R.id.moreActionsButton -> when {
                        isViewVisible(R.id.songCastButton) -> R.id.songCastButton
                        else -> R.id.autoscrollButton
                    }
                    R.id.songCastButton -> R.id.autoscrollButton
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
            }
            currentViewId == R.id.main_content -> R.id.navMenuButton
            currentViewId == R.id.navMenuButton -> {
                navigationMenuController.navDrawerShow()
                -1
            }
            layoutController.isState(LatestSongsLayoutController::class) -> when (currentViewId) {
                R.id.updateLatestSongs -> R.id.navMenuButton
                else -> R.id.navMenuButton
            }
            layoutController.isState(CustomSongsListLayoutController::class) -> when (currentViewId) {
                R.id.moreActionsButton -> R.id.songsSortButton
                R.id.songsSortButton -> R.id.searchSongButton
                R.id.searchSongButton -> when {
                    isViewVisible(R.id.goBackButton) -> R.id.goBackButton
                    else -> R.id.navMenuButton
                }
                R.id.searchFilterClearButton -> R.id.searchFilterEdit
                R.id.searchFilterEdit -> when {
                    isViewVisible(R.id.goBackButton) -> R.id.goBackButton
                    else -> R.id.navMenuButton
                }
                R.id.goBackButton -> R.id.navMenuButton
                else -> R.id.navMenuButton
            }
            layoutController.isState(EditSongLayoutController::class) -> when (currentViewId) {
                R.id.moreActionsButton -> R.id.saveSongButton
                R.id.saveSongButton -> R.id.goBackButton
                R.id.goBackButton -> R.id.navMenuButton
                else -> 0
            }
            layoutController.isState(ChordsEditorLayoutController::class) -> when (currentViewId) {
                R.id.goBackButton -> R.id.navMenuButton
                R.id.tooltipEditChordsLyricsInfo -> R.id.goBackButton
                else -> 0
            }
            layoutController.isState(PlaylistLayoutController::class) -> when (currentViewId) {
                R.id.goBackButton -> R.id.navMenuButton
                R.id.addPlaylistButton -> when {
                    isViewVisible(R.id.goBackButton) -> R.id.goBackButton
                    else -> R.id.navMenuButton
                }
                else -> R.id.navMenuButton
            }
            currentViewId == R.id.languageFilterButton -> when {
                isViewVisible(R.id.goBackButton) -> R.id.goBackButton
                else -> R.id.navMenuButton
            }
            currentViewId == R.id.searchSongButton -> when {
                isViewVisible(R.id.languageFilterButton) -> R.id.languageFilterButton
                isViewVisible(R.id.goBackButton) -> R.id.goBackButton
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
        } else if (uiInfoService.isSnackbarShown()) {
            if (uiInfoService.focusSnackBar())
                return -1
        }

        return when {
            layoutController.isState(SongPreviewLayoutController::class) -> when (currentViewId) {
                R.id.main_content, R.id.overlayScrollView -> when {
                    songPreviewLayoutController.isTransposePanelVisible() -> R.id.transpose0Button
                    songPreviewLayoutController.isAutoscrollPanelVisible() -> R.id.autoscrollToggleButton
                    songPreviewLayoutController.canScrollDown() -> {
                        songPreviewLayoutController.scrollByStep(+1)
                        -1
                    }
                    else -> {
                        songPreviewLayoutController.scrollByStep(+1)
                        R.id.navMenuButton
                    }
                }
                R.id.navMenuButton -> {
                    songPreviewLayoutController.scrollByStep(+1)
                    R.id.main_content
                }
                R.id.songInfoButton, R.id.chordsHelpButton, R.id.setFavouriteButton,
                R.id.transposeButton, R.id.autoscrollButton, R.id.songCastButton, R.id.moreActionsButton -> when {
                    songPreviewLayoutController.isTransposePanelVisible() -> R.id.transpose0Button
                    songPreviewLayoutController.isAutoscrollPanelVisible() -> R.id.autoscrollToggleButton
                    songPreviewLayoutController.canScrollDown() -> R.id.main_content
                    else -> {
                        songPreviewLayoutController.scrollByStep(+1)
                        R.id.navMenuButton
                    }
                }
                R.id.autoscrollToggleButton -> R.id.speedSeekbar
                else -> 0
            }
            currentViewId == R.id.main_content -> R.id.navMenuButton
            layoutController.isState(TopSongsLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton, R.id.languageFilterButton, R.id.searchSongButton -> R.id.itemsList
                else -> 0
            }
            layoutController.isState(SongTreeLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton, R.id.goBackButton, R.id.languageFilterButton, R.id.searchSongButton -> R.id.itemsList
                else -> 0
            }
            layoutController.isState(SongSearchLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton, R.id.searchFilterEdit, R.id.searchFilterClearButton -> R.id.itemsList
                else -> 0
            }
            layoutController.isState(PlaylistFillLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton, R.id.searchFilterEdit, R.id.searchFilterClearButton -> R.id.itemsList
                else -> 0
            }
            layoutController.isState(CustomSongsListLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton, R.id.goBackButton, R.id.searchFilterEdit, R.id.searchFilterClearButton, R.id.searchSongButton, R.id.songsSortButton, R.id.moreActionsButton -> R.id.itemsListView
                else -> 0
            }
            layoutController.isState(EditSongLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton, R.id.goBackButton, R.id.saveSongButton, R.id.moreActionsButton -> R.id.songTitleEdit
                R.id.songTitleEdit -> R.id.customCategoryNameEdit
                R.id.customCategoryNameEdit -> R.id.songChordNotationSpinner
                R.id.songChordNotationSpinner -> R.id.tooltipEditChordsLyricsInfo
                R.id.tooltipEditChordsLyricsInfo -> R.id.songContentEdit
                else -> 0
            }
            layoutController.isState(ChordsEditorLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton, R.id.goBackButton, R.id.tooltipEditChordsLyricsInfo -> R.id.transformChordsButton
                else -> 0
            }
            layoutController.isState(LatestSongsLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton, R.id.updateLatestSongs -> R.id.itemsList
                else -> 0
            }
            layoutController.isState(FavouritesLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> R.id.itemsList
                else -> 0
            }
            layoutController.isState(PlaylistLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton, R.id.goBackButton, R.id.addPlaylistButton -> R.id.compose_view
                else -> 0
            }
            layoutController.isState(OpenHistoryLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> R.id.itemsList
                else -> 0
            }
            layoutController.isState(MissingSongLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> R.id.missingSongMessageEdit
                else -> 0
            }
            layoutController.isState(PublishSongLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> R.id.publishSongTitleEdit
                else -> 0
            }
            layoutController.isState(RoomListLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton, R.id.moreActionsButton -> R.id.myNameEditText
                else -> 0
            }
            layoutController.isState(RoomLobbyLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> R.id.openSelectedSongButton
                else -> 0
            }
            layoutController.isState(BillingLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> R.id.billingBuyAdFree
                else -> 0
            }
            layoutController.isState(ContactLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> R.id.contactSubjectEdit
                else -> 0
            }
            layoutController.isState(SettingsLayoutController::class) -> when (currentViewId) {
                R.id.navMenuButton -> R.id.fragment_content
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
                R.id.main_content, R.id.overlayScrollView -> when {
                    songPreviewLayoutController.isTransposePanelVisible() -> R.id.transpose0Button
                    songPreviewLayoutController.isAutoscrollPanelVisible() -> R.id.autoscrollToggleButton
                    songPreviewLayoutController.canScrollUp() -> {
                        songPreviewLayoutController.scrollByStep(-1)
                        -1
                    }
                    else -> {
                        songPreviewLayoutController.scrollByStep(-1)
                        R.id.navMenuButton
                    }
                }
                R.id.navMenuButton -> {
                    songPreviewLayoutController.scrollByStep(-1)
                    R.id.navMenuButton
                }
                R.id.songInfoButton, R.id.chordsHelpButton, R.id.setFavouriteButton,
                R.id.transposeButton, R.id.autoscrollButton, R.id.songCastButton, R.id.moreActionsButton -> R.id.navMenuButton
                R.id.transposeM5Button, R.id.transposeM1Button, R.id.transpose0Button,
                R.id.transposeP1Button, R.id.transposeP5Button -> R.id.transposeButton
                R.id.autoscrollToggleButton -> R.id.autoscrollButton
                R.id.speedMinusButton, R.id.speedSeekbar, R.id.speedPlusButton -> R.id.autoscrollToggleButton
                else -> 0
            }
            currentViewId == R.id.main_content -> R.id.navMenuButton
            currentViewId == R.id.navMenuButton -> R.id.navMenuButton

            layoutController.isState(EditSongLayoutController::class) -> when (currentViewId) {
                R.id.songContentEdit -> R.id.tooltipEditChordsLyricsInfo
                R.id.tooltipEditChordsLyricsInfo -> R.id.songChordNotationSpinner
                R.id.songChordNotationSpinner -> R.id.customCategoryNameEdit
                R.id.customCategoryNameEdit -> R.id.songTitleEdit
                R.id.songTitleEdit -> R.id.navMenuButton
                else -> 0
            }
            layoutController.isState(ChordsEditorLayoutController::class) -> when (currentViewId) {
                R.id.transformChordsButton -> R.id.navMenuButton
                else -> 0
            }
            layoutController.isState(LatestSongsLayoutController::class) -> when (currentViewId) {
                R.id.itemsList -> R.id.navMenuButton
                else -> 0
            }
            layoutController.isState(FavouritesLayoutController::class) -> when (currentViewId) {
                R.id.itemsList -> R.id.navMenuButton
                else -> 0
            }
            layoutController.isState(PlaylistLayoutController::class) -> when (currentViewId) {
                R.id.compose_view -> R.id.navMenuButton
                else -> 0
            }
            layoutController.isState(OpenHistoryLayoutController::class) -> when (currentViewId) {
                R.id.itemsList -> R.id.navMenuButton
                else -> 0
            }
            layoutController.isState(MissingSongLayoutController::class) -> when (currentViewId) {
                R.id.missingSongMessageEdit -> R.id.navMenuButton
                else -> 0
            }
            layoutController.isState(PublishSongLayoutController::class) -> when (currentViewId) {
                R.id.publishSongTitleEdit -> R.id.navMenuButton
                else -> 0
            }
            layoutController.isState(RoomListLayoutController::class) -> when (currentViewId) {
                R.id.myNameEditText -> R.id.navMenuButton
                else -> 0
            }
            layoutController.isState(RoomLobbyLayoutController::class) -> when (currentViewId) {
                R.id.openSelectedSongButton -> R.id.navMenuButton
                else -> 0
            }
            layoutController.isState(BillingLayoutController::class) -> when (currentViewId) {
                R.id.billingBuyAdFree -> R.id.navMenuButton
                else -> 0
            }
            layoutController.isState(ContactLayoutController::class) -> when (currentViewId) {
                R.id.contactSubjectEdit -> R.id.navMenuButton
                else -> 0
            }
            layoutController.isState(SettingsLayoutController::class) -> when (currentViewId) {
                R.id.fragment_content -> R.id.navMenuButton
                else -> 0
            }
            else -> 0
        }
    }

    private fun isViewVisible(resId: Int): Boolean {
        return activity.findViewById<View>(resId)?.isVisible == true
    }
}