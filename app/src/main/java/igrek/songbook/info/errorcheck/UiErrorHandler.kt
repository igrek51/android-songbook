package igrek.songbook.info.errorcheck


import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory


class UiErrorHandler(
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val uiResourceService by LazyExtractor(uiResourceService)

    fun handleError(t: Throwable) {
        LoggerFactory.logger.error(t)
        val err: String = when {
            t.message != null -> t.message
            else -> t::class.simpleName
        }.orEmpty()
        val message = uiResourceService.resString(R.string.error_occurred_s, err)
        uiInfoService.showInfoWithAction(message, R.string.error_details) {
            showDetails(t)
        }
    }

    private fun showDetails(t: Throwable) {
        val message = "${t::class.simpleName}\n${t.message}"
        uiInfoService.dialog(titleResId = R.string.error_occurred, message = message)
    }

    companion object {
        fun handleError(t: Throwable) {
            UiErrorHandler().handleError(t)
        }
    }

}
