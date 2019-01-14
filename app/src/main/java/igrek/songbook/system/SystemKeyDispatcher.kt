package igrek.songbook.system

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.layout.LayoutController
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import javax.inject.Inject

class SystemKeyDispatcher {

    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var autoscrollService: AutoscrollService

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun onKeyBack(): Boolean {
        layoutController.onBackClicked()
        return true
    }

    fun onKeyMenu(): Boolean {
        return false
    }

    fun onVolumeUp(): Boolean {
        return autoscrollService.onVolumeUp()
    }

    fun onVolumeDown(): Boolean {
        return autoscrollService.onVolumeDown()
    }
}
