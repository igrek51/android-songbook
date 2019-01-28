package igrek.songbook.system

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.LayoutState
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import javax.inject.Inject

class SystemKeyDispatcher {

    @Inject
    lateinit var layoutController: Lazy<LayoutController>
    @Inject
    lateinit var autoscrollService: Lazy<AutoscrollService>

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun onKeyBack(): Boolean {
        layoutController.get().onBackClicked()
        return true
    }

    fun onKeyMenu(): Boolean {
        return false
    }

    fun onVolumeUp(): Boolean {
        if (!layoutController.get().isState(LayoutState.SONG_PREVIEW))
            return false
        return autoscrollService.get().onVolumeUp()
    }

    fun onVolumeDown(): Boolean {
        if (!layoutController.get().isState(LayoutState.SONG_PREVIEW))
            return false
        return autoscrollService.get().onVolumeDown()
    }
}
