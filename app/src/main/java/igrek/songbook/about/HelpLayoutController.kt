package igrek.songbook.about

import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import javax.inject.Inject

class HelpLayoutController {

    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var uiResourceService: UiResourceService

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun showUIHelp() {
        val message = uiResourceService.resString(R.string.ui_help_content)
        val title = uiResourceService.resString(R.string.nav_help)
        uiInfoService.showDialog(title, message)
    }
}
