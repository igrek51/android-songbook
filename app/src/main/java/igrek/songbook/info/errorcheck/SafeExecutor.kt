package igrek.songbook.info.errorcheck


import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import javax.inject.Inject

class SafeExecutor(action: () -> Unit) {

    @Inject
    lateinit var uiInfoService: Lazy<UiInfoService>
    @Inject
    lateinit var uiResourceService: Lazy<UiResourceService>

    init {
        execute(action)
    }

    private fun execute(action: () -> Unit) {
        try {
            action.invoke()
        } catch (t: Throwable) {
            LoggerFactory.logger.error(t)
            DaggerIoc.factoryComponent.inject(this)
            val err: String? = when {
                t.message != null -> t.message
                else -> t::class.simpleName
            }
            uiInfoService.get().showInfo(uiResourceService.get().resString(R.string.error_occurred, err))
        }
    }

}
