package igrek.songbook.settings.enums

import igrek.songbook.chords.diagram.guitar.ChordDiagramStyle
import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.preferences.SettingsState

class SettingsEnumService(
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
) {
    private val uiResourceService by LazyExtractor(uiResourceService)
    val preferencesState by LazyExtractor(settingsState)

    fun instrumentEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        for (item in ChordsInstrument.values()) {
            val displayName = uiResourceService.resString(item.displayNameResId)
            map[item.id.toString()] = displayName
        }
        return map
    }

    fun chordDiagramStyleEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        for (item in ChordDiagramStyle.values()) {
            map[item.id.toString()] = uiResourceService.resString(item.nameResId)
        }
        return map
    }

    fun customSongsOrderingEnumEntries(): LinkedHashMap<CustomSongsOrdering, String> {
        val map = LinkedHashMap<CustomSongsOrdering, String>()
        CustomSongsOrdering.values().forEach { item ->
            map[item] = uiResourceService.resString(item.nameResId)
        }
        return map
    }

    fun customSongsOrderingStringEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        CustomSongsOrdering.values().forEach { item ->
            map[item.id.toString()] = uiResourceService.resString(item.nameResId)
        }
        return map
    }
}
