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

    fun handleError(t: Throwable, contextResId: Int = R.string.error_occurred_s) {
        LoggerFactory.logger.error(t)
        val err: String = when {
            t.message != null -> t.message
            else -> t::class.simpleName
        }.orEmpty()
        uiInfoService.showInfoAction(contextResId, err, indefinite = true, actionResId = R.string.error_details) {
            showDetails(t)
        }
    }

    private fun showDetails(t: Throwable) {
        val message = "${t::class.simpleName}\n${t.message.orEmpty()}"
        uiInfoService.dialog(titleResId = R.string.error_occurred, message = message)
    }

    companion object {
        fun handleError(t: Throwable, contextResId: Int = R.string.error_occurred_s) {
            UiErrorHandler().handleError(t, contextResId)
        }
    }

}
