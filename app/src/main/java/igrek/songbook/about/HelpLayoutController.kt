package igrek.songbook.about

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

class HelpLayoutController(
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)

    fun showUIHelp() {
        uiInfoService.dialog(R.string.nav_help, R.string.ui_help_content)
    }
}
