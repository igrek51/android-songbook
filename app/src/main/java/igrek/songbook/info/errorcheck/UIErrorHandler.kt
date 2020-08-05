package igrek.songbook.info.errorcheck


import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory


class UIErrorHandler private constructor(
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val uiResourceService by LazyExtractor(uiResourceService)

    private val logger = LoggerFactory.logger

    private fun handleError(t: Throwable) {
        logger.error(t)
        uiInfoService.showInfo(uiResourceService.resString(R.string.error_occurred_s, t.message))
    }

    companion object {
        fun showError(t: Throwable) {
            UIErrorHandler().handleError(t)
        }
    }

}
