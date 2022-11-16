package igrek.songbook.info.errorcheck

import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

class LocalizedError(
    val messageRes: Int,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
) : RuntimeException() {
    private val uiResourceService by LazyExtractor(uiResourceService)

    override val message: String
        get() {
            return uiResourceService.resString(messageRes)
        }
}