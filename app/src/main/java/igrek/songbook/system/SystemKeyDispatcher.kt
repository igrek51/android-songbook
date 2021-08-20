package igrek.songbook.system

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

    fun onKeyBack(): Boolean {
        layoutController.onBackClicked()
        return true
    }

    fun onKeyMenu(): Boolean {
        return false
    }

    fun onVolumeUp(): Boolean {
        if (!layoutController.isState(SongPreviewLayoutController::class))
            return false
        return autoscrollService.onVolumeUp()
    }

    fun onVolumeDown(): Boolean {
        if (!layoutController.isState(SongPreviewLayoutController::class))
            return false
        return autoscrollService.onVolumeDown()
    }

    fun onArrowUp(): Boolean {
        if (!layoutController.isState(SongPreviewLayoutController::class))
            return false
        return songPreviewLayoutController.scrollByStep(-1)
    }

    fun onArrowDown(): Boolean {
        if (!layoutController.isState(SongPreviewLayoutController::class))
            return false
        return songPreviewLayoutController.scrollByStep(+1)
    }

    fun onArrowLeft(): Boolean {
        if (!layoutController.isState(SongPreviewLayoutController::class))
            return false
        return playlistLayoutController.goToNextOrPrevious(-1)
    }

    fun onArrowRight(): Boolean {
        if (!layoutController.isState(SongPreviewLayoutController::class))
            return false
        return playlistLayoutController.goToNextOrPrevious(+1)
    }
}
