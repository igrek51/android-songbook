package igrek.songbook.about

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

class HelpLayoutController(
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val uiResourceService by LazyExtractor(uiResourceService)

    fun showUIHelp() {
        val message = uiResourceService.resString(R.string.ui_help_content)
        val title = uiResourceService.resString(R.string.nav_help)
        uiInfoService.showDialog(title, message)
    }
}
