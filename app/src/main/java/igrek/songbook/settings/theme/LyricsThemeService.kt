package igrek.songbook.settings.theme

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.settings.preferences.PreferencesDefinition
import igrek.songbook.settings.preferences.PreferencesService
import java.util.*
import javax.inject.Inject

class LyricsThemeService {

    @Inject
    lateinit var preferencesService: PreferencesService
    @Inject
    lateinit var uiResourceService: UiResourceService

    var fontsize: Float = 0f
        set(value) {
            field = if (value < 1)
                1f
            else
                value
        }

    var fontTypeface: FontTypeface? = null
    var colorScheme: ColorScheme? = null
    var chordsEndOfLine = false

    init {
        DaggerIoc.factoryComponent.inject(this)
        loadPreferences()
    }

    private fun loadPreferences() {
        fontsize = preferencesService.getValue(PreferencesDefinition.Fontsize, Float::class.java)!!

        val fontTypefaceId = preferencesService.getValue(PreferencesDefinition.FontTypefaceId, String::class.java)
        if (fontTypefaceId != null) {
            fontTypeface = FontTypeface.parseById(fontTypefaceId)
        }

        val colorSchemeId = preferencesService.getValue(PreferencesDefinition.ColorSchemeId, Long::class.java)
        if (colorSchemeId != null) {
            colorScheme = ColorScheme.parseById(colorSchemeId)
        }

        chordsEndOfLine = preferencesService.getValue(PreferencesDefinition.ChordsEndOfLine, Boolean::class.java)
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