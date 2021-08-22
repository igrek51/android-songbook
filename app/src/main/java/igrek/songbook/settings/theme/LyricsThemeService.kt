package igrek.songbook.settings.theme

import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.preferences.PreferencesState
import java.util.*

class LyricsThemeService(
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
        preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
) {
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val preferencesState by LazyExtractor(preferencesState)

    var fontsize: Float
        get() = preferencesState.fontsize
        set(value) {
            preferencesState.fontsize = if (value < 1)
                1f
            else
                value
        }
    var fontTypeface: FontTypeface
        get() = preferencesState.fontTypeface
        set(value) {
            preferencesState.fontTypeface = value
        }
    var colorScheme: ColorScheme
        get() = preferencesState.colorScheme
        set(value) {
            preferencesState.colorScheme = value
        }
    var displayStyle: DisplayStyle
        get() = preferencesState.chordsDisplayStyle
        set(value) {
            preferencesState.chordsDisplayStyle = value
        }
    var horizontalScroll: Boolean
        get() = preferencesState.horizontalScroll
        set(value) {
            preferencesState.horizontalScroll = value
        }

    fun fontTypefaceEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        for (item in FontTypeface.values()) {
            val displayName = uiResourceService.resString(item.displayNameResId)
            map[item.id] = displayName
        }
        return map
    }

    fun colorSchemeEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        for (item in ColorScheme.values()) {
            val displayName = uiResourceService.resString(item.displayNameResId)
            map[item.id.toString()] = displayName
        }
        return map
    }

    fun displayStyleEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        for (item in DisplayStyle.values()) {
            val displayName = uiResourceService.resString(item.nameResId)
            map[item.id.toString()] = displayName
        }
        return map
    }

}