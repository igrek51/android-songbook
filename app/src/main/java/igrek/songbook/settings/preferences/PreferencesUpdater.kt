package igrek.songbook.settings.preferences

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.language.AppLanguage
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.songpreview.theme.ColorScheme
import igrek.songbook.songpreview.theme.FontTypeface
import igrek.songbook.songpreview.theme.LyricsThemeService
import javax.inject.Inject

class PreferencesUpdater {

    @Inject
    lateinit var preferencesService: PreferencesService
    @Inject
    lateinit var lyricsThemeService: Lazy<LyricsThemeService>
    @Inject
    lateinit var autoscrollService: Lazy<AutoscrollService>
    @Inject
    lateinit var appLanguageService: Lazy<AppLanguageService>
    @Inject
    lateinit var chordsNotationService: Lazy<ChordsNotationService>

    // preferences getters / setters proxy
    var appLanguage: AppLanguage?
        get() = appLanguageService.get().appLanguage
        set(value) {
            appLanguageService.get().appLanguage = value
        }

    var chordsNotation: ChordsNotation?
        get() = chordsNotationService.get().chordsNotation
        set(value) {
            chordsNotationService.get().chordsNotation = value
        }

    var fontsize: Float
        get() = lyricsThemeService.get().fontsize
        set(value) {
            lyricsThemeService.get().fontsize = value
        }

    var fontTypeface: FontTypeface?
        get() = lyricsThemeService.get().fontTypeface
        set(value) {
            lyricsThemeService.get().fontTypeface = value
        }

    var colorScheme: ColorScheme?
        get() = lyricsThemeService.get().colorScheme
        set(value) {
            lyricsThemeService.get().colorScheme = value
        }

    var autoscrollInitialPause: Long
        get() = autoscrollService.get().initialPause
        set(value) {
            autoscrollService.get().initialPause = value
        }

    var autoscrollSpeed: Float
        get() = autoscrollService.get().autoscrollSpeed
        set(value) {
            autoscrollService.get().autoscrollSpeed = value
        }

    var excludedLanguages: List<AppLanguage>
        get() = appLanguageService.get().excludedLanguages
        set(value) {
            appLanguageService.get().excludedLanguages = value
        }

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun updateAndSave() {
        preferencesService.setValue(PreferencesDefinition.fontsize, fontsize)
        preferencesService.setValue(PreferencesDefinition.fontTypefaceId, fontTypeface?.id)
        preferencesService.setValue(PreferencesDefinition.colorSchemeId, colorScheme?.id)

        preferencesService.setValue(PreferencesDefinition.autoscrollInitialPause, autoscrollInitialPause)
        preferencesService.setValue(PreferencesDefinition.autoscrollSpeed, autoscrollSpeed)

        preferencesService.setValue(PreferencesDefinition.chordsNotationId, chordsNotation?.id)

        preferencesService.setValue(PreferencesDefinition.appLanguage, appLanguage?.langCode)
        preferencesService.setValue(PreferencesDefinition.excludedLanguages,
                appLanguageService.get().lanugages2String(excludedLanguages))

        preferencesService.saveAll()
    }

}