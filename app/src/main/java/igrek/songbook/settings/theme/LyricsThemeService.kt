package igrek.songbook.settings.theme

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.settings.preferences.PreferencesField
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

    var fontsize: Float = 0f
        get() = preferencesState.fontsize
        set(value) {
            field = if (value < 1)
                1f
            else
                value
            preferencesState.fontsize = value
        }

    var fontTypeface: FontTypeface? = null
    var colorScheme: ColorScheme? = null
    var chordsEndOfLine = false
    var chordsAbove = false

    init {
        DaggerIoc.factoryComponent.inject(this)
        loadPreferences()
    }

    private fun loadPreferences() {
        fontsize = preferencesService.getValue(PreferencesField.Fontsize, Float::class)!!

        val fontTypefaceId = preferencesService.getValue(PreferencesField.FontTypefaceId, String::class)
        if (fontTypefaceId != null) {
            fontTypeface = FontTypeface.parseById(fontTypefaceId)
        }

        val colorSchemeId = preferencesService.getValue(PreferencesField.ColorSchemeId, Long::class)
        if (colorSchemeId != null) {
            colorScheme = ColorScheme.parseById(colorSchemeId)
        }

        chordsEndOfLine = preferencesService.getValue(PreferencesField.ChordsEndOfLine, Boolean::class)
                ?: false
        chordsAbove = preferencesService.getValue(PreferencesField.ChordsAbove, Boolean::class)
                ?: false
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