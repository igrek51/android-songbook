package igrek.songbook.system

import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songpreview.autoscroll.AutoscrollService

class SystemKeyDispatcher(
        layoutController: LazyInject<LayoutController> = appFactory.layoutController,
        autoscrollService: LazyInject<AutoscrollService> = appFactory.autoscrollService,
) {
    private val layoutController by LazyExtractor(layoutController)
    private val autoscrollService by LazyExtractor(autoscrollService)

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
}
