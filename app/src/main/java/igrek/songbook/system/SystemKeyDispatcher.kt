package igrek.songbook.system

import android.view.KeyEvent
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.NextFocusTraverser
import igrek.songbook.playlist.PlaylistService
import igrek.songbook.settings.buttons.MediaButtonBehaviours
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songpreview.autoscroll.AutoscrollService

class SystemKeyDispatcher(
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    autoscrollService: LazyInject<AutoscrollService> = appFactory.autoscrollService,
    songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
    playlistService: LazyInject<PlaylistService> = appFactory.playlistService,
    nextFocusTraverser: LazyInject<NextFocusTraverser> = appFactory.nextFocusTraverser,
) {
    private val layoutController by LazyExtractor(layoutController)
    private val autoscrollService by LazyExtractor(autoscrollService)
    private val songPreviewLayoutController by LazyExtractor(songPreviewLayoutController)
    private val preferencesState by LazyExtractor(preferencesState)
    private val playlistService by LazyExtractor(playlistService)
    private val nextFocusTraverser by LazyExtractor(nextFocusTraverser)

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
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_MEDIA_REWIND,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD,
            KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD -> {
                return onArrowLeft()
            }
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD,
            KeyEvent.KEYCODE_MEDIA_STEP_FORWARD -> {
                return onArrowRight()
            }
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER -> {
                return onEnterKey()
            }
            KeyEvent.KEYCODE_HEADSETHOOK, // mini jack headset button
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                return onMediaButton()
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
        if (layoutController.isState(SongPreviewLayoutController::class))
            return autoscrollService.onVolumeUp()
        return false
    }

    private fun onVolumeDown(): Boolean {
        if (layoutController.isState(SongPreviewLayoutController::class))
            return autoscrollService.onVolumeDown()
        return false
    }

    private fun onArrowUp(): Boolean {
        if (layoutController.isState(SongPreviewLayoutController::class))
            return songPreviewLayoutController.scrollByStep(-1)

        if (nextFocusTraverser.moveToNextView(nextFocusTraverser::nextUpView))
            return true

        return false
    }

    private fun onArrowDown(): Boolean {
        if (layoutController.isState(SongPreviewLayoutController::class))
            return songPreviewLayoutController.scrollByStep(+1)

        if (nextFocusTraverser.moveToNextView(nextFocusTraverser::nextDownView))
            return true

        return false
    }

    private fun onArrowLeft(): Boolean {
        if (layoutController.isState(SongPreviewLayoutController::class)) {
            playlistService.goToNextOrPrevious(-1)
            return true
        }

        if (nextFocusTraverser.moveToNextView(nextFocusTraverser::nextLeftView))
            return true

        return false
    }

    private fun onArrowRight(): Boolean {
        if (layoutController.isState(SongPreviewLayoutController::class)) {
            playlistService.goToNextOrPrevious(+1)
            return true
        }

        if (nextFocusTraverser.moveToNextView(nextFocusTraverser::nextRightView))
            return true

        return false
    }

    private fun onEnterKey(): Boolean {
        return false
    }

    private fun onMediaButton(): Boolean {
        if (!layoutController.isState(SongPreviewLayoutController::class))
            return false
        when (preferencesState.mediaButtonBehaviour) {
            MediaButtonBehaviours.DO_NOTHING -> {
                return false
            }
            MediaButtonBehaviours.SCROLL_DOWN -> {
                songPreviewLayoutController.scrollByStep(+1)
            }
            MediaButtonBehaviours.NEXT_SONG -> {
                playlistService.goToNextOrPrevious(+1)
            }
            MediaButtonBehaviours.SCROLL_DOWN_NEXT_SONG -> {
                if (songPreviewLayoutController.canScrollDown()) {
                    songPreviewLayoutController.scrollByStep(+1)
                } else {
                    playlistService.goToNextOrPrevious(+1)
                }
            }
        }
        return true
    }
}
