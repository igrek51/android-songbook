package igrek.songbook.system

import android.view.KeyEvent
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songpreview.autoscroll.AutoscrollService

class SystemKeyDispatcher(
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    autoscrollService: LazyInject<AutoscrollService> = appFactory.autoscrollService,
    songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
    playlistLayoutController: LazyInject<PlaylistLayoutController> = appFactory.playlistLayoutController,
) {
    private val layoutController by LazyExtractor(layoutController)
    private val autoscrollService by LazyExtractor(autoscrollService)
    private val songPreviewLayoutController by LazyExtractor(songPreviewLayoutController)
    private val playlistLayoutController by LazyExtractor(playlistLayoutController)

    fun onKeyDown(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                return onKeyBack()
            }
            KeyEvent.KEYCODE_MENU -> {
                return onKeyMenu()
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                return onVolumeUp()
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                return onVolumeDown()
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                return onArrowUp()
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                return onArrowDown()
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                return onArrowLeft()
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                return onArrowRight()
            }
        }
        return false
    }

    private fun onKeyBack(): Boolean {
        layoutController.onBackClicked()
        return true
    }

    private fun onKeyMenu(): Boolean {
        return false
    }

    private fun onVolumeUp(): Boolean {
        if (!layoutController.isState(SongPreviewLayoutController::class))
            return false
        return autoscrollService.onVolumeUp()
    }

    private fun onVolumeDown(): Boolean {
        if (!layoutController.isState(SongPreviewLayoutController::class))
            return false
        return autoscrollService.onVolumeDown()
    }

    private fun onArrowUp(): Boolean {
        if (!layoutController.isState(SongPreviewLayoutController::class))
            return false
        return songPreviewLayoutController.scrollByStep(-1)
    }

    private fun onArrowDown(): Boolean {
        if (!layoutController.isState(SongPreviewLayoutController::class))
            return false
        return songPreviewLayoutController.scrollByStep(+1)
    }

    private fun onArrowLeft(): Boolean {
        if (!layoutController.isState(SongPreviewLayoutController::class))
            return false
        return playlistLayoutController.goToNextOrPrevious(-1)
    }

    private fun onArrowRight(): Boolean {
        if (!layoutController.isState(SongPreviewLayoutController::class))
            return false
        return playlistLayoutController.goToNextOrPrevious(+1)
    }
}
