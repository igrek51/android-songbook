package igrek.songbook.info.errorcheck


import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import javax.inject.Inject

class SafeExecutor {

    @Inject
    lateinit var uiInfoService: Lazy<UiInfoService>
    @Inject
    lateinit var uiResourceService: Lazy<UiResourceService>

    fun execute(action: () -> Unit) {
        try {
            action.invoke()
        } catch (t: Throwable) {
            LoggerFactory.logger.error(t)
            DaggerIoc.getFactoryComponent().inject(this)
            uiInfoService.get().showInfo(uiResourceService.get().resString(R.string.error_occurred, t.message))
        }
    }

}
