package igrek.songbook.info.errorcheck


import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

class SafeExecutor(
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
        action: () -> Unit,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val uiResourceService by LazyExtractor(uiResourceService)

    init {
        execute(action)
    }

    private fun execute(action: () -> Unit) {
        try {
            action.invoke()
        } catch (t: Throwable) {
            LoggerFactory.logger.error(t)
            val err: String? = when {
                t.message != null -> t.message
                else -> t::class.simpleName
            }
            uiInfoService.showInfo(uiResourceService.resString(R.string.error_occurred, err))
        }
    }

}
