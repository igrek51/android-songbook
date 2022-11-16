package igrek.songbook.settings.buttons


import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

class MediaButtonService(
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
) {
    private val uiResourceService by LazyExtractor(uiResourceService)

    fun mediaButtonBehavioursEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        for (item in MediaButtonBehaviours.values()) {
            val displayName = uiResourceService.resString(item.nameResId)
            map[item.id.toString()] = displayName
        }
        return map
    }

}