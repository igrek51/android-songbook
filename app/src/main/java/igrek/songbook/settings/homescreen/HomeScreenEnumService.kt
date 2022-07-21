package igrek.songbook.settings.homescreen


import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

class HomeScreenEnumService(
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
) {
    private val uiResourceService by LazyExtractor(uiResourceService)

    fun homeScreenEnumsEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        for (item in HomeScreenEnum.values()) {
            val displayName = uiResourceService.resString(item.nameResId)
            map[item.id.toString()] = displayName
        }
        return map
    }

}