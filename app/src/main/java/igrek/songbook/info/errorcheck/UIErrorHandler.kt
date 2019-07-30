package igrek.songbook.info.errorcheck


import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import javax.inject.Inject

class UIErrorHandler private constructor() {

    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var uiResourceService: UiResourceService

    private val logger = LoggerFactory.logger

    private fun handleError(t: Throwable) {
        DaggerIoc.factoryComponent.inject(this)
        logger.error(t)
        uiInfoService.showInfo(uiResourceService.resString(R.string.error_occurred, t.message))
    }

    companion object {

        fun showError(t: Throwable) {
            UIErrorHandler().handleError(t)
        }
    }

}
