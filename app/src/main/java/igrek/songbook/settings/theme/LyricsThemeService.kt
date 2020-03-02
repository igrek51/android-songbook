package igrek.songbook.settings.theme

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
import java.util.*
import javax.inject.Inject

class LyricsThemeService {

    @Inject
    lateinit var preferencesService: PreferencesService
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var preferencesState: PreferencesState

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
    var chordsEndOfLine: Boolean
        get() = preferencesState.chordsEndOfLine
        set(value) {
            preferencesState.chordsEndOfLine = value
        }
    var chordsAbove: Boolean
        get() = preferencesState.chordsAbove
        set(value) {
            preferencesState.chordsAbove = value
        }

    init {
        DaggerIoc.factoryComponent.inject(this)
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

}