package igrek.songbook.settings.preferences

import dagger.Lazy
import igrek.songbook.admin.AdminService
import igrek.songbook.custom.CustomSongService
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.instrument.ChordsInstrument
import igrek.songbook.settings.instrument.ChordsInstrumentService
import igrek.songbook.settings.language.AppLanguage
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.FontTypeface
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.songpreview.lyrics.LyricsManager
import igrek.songbook.songselection.random.RandomSongOpener
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PreferencesState {

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
    @Inject
    lateinit var randomSongOpener: Lazy<RandomSongOpener>
    @Inject
    lateinit var customSongService: Lazy<CustomSongService>
    @Inject
    lateinit var lyricsManager: Lazy<LyricsManager>
    @Inject
    lateinit var chordsInstrumentService: Lazy<ChordsInstrumentService>
    @Inject
    lateinit var adminService: Lazy<AdminService>

    private val logger = LoggerFactory.logger

    // preferences getters / setters proxy
    var fontsize: Float by PreferenceDelegate(PreferencesField.Fontsize)

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

    var chordsEndOfLine: Boolean
        get() = lyricsThemeService.get().chordsEndOfLine
        set(value) {
            lyricsThemeService.get().chordsEndOfLine = value
        }

    var chordsAbove: Boolean
        get() = lyricsThemeService.get().chordsAbove
        set(value) {
            lyricsThemeService.get().chordsAbove = value
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

    var autoscrollSpeedAutoAdjustment: Boolean
        get() = autoscrollService.get().autoSpeedAdjustment
        set(value) {
            autoscrollService.get().autoSpeedAdjustment = value
        }

    var autoscrollSpeedVolumeKeys: Boolean
        get() = autoscrollService.get().volumeKeysSpeedControl
        set(value) {
            autoscrollService.get().volumeKeysSpeedControl = value
        }

    var randomFavouriteSongsOnly: Boolean
        get() = randomSongOpener.get().fromFavouriteSongsOnly
        set(value) {
            randomSongOpener.get().fromFavouriteSongsOnly = value
        }

    var customSongsGroupCategories: Boolean
        get() = customSongService.get().customSongsGroupCategories
        set(value) {
            customSongService.get().customSongsGroupCategories = value
        }

    var restoreTransposition: Boolean
        get() = lyricsManager.get().restoreTransposition
        set(value) {
            lyricsManager.get().restoreTransposition = value
        }

    var chordsInstrument: ChordsInstrument?
        get() = chordsInstrumentService.get().instrument
        set(value) {
            chordsInstrumentService.get().instrument = value ?: ChordsInstrument.default
        }

    var userAuthToken: String
        get() = adminService.get().userAuthToken
        set(value) {
            adminService.get().userAuthToken = value
        }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun updateAndSave() {
        preferencesService.setValue(PreferencesField.Fontsize, fontsize)
        preferencesService.setValue(PreferencesField.FontTypefaceId, fontTypeface)
        preferencesService.setValue(PreferencesField.ColorSchemeId, colorScheme)
        preferencesService.setValue(PreferencesField.ChordsEndOfLine, chordsEndOfLine)
        preferencesService.setValue(PreferencesField.ChordsAbove, chordsAbove)

        preferencesService.setValue(PreferencesField.AutoscrollInitialPause, autoscrollInitialPause)
        preferencesService.setValue(PreferencesField.AutoscrollSpeed, autoscrollSpeed)
        preferencesService.setValue(PreferencesField.AutoscrollSpeedAutoAdjustment, autoscrollSpeedAutoAdjustment)
        preferencesService.setValue(PreferencesField.AutoscrollSpeedVolumeKeys, autoscrollSpeedVolumeKeys)

        preferencesService.setValue(PreferencesField.AppLanguage, appLanguage)
        preferencesService.setValue(PreferencesField.ChordsNotationId, chordsNotation)
        preferencesService.setValue(PreferencesField.ChordsInstrument, chordsInstrument)

        preferencesService.setValue(PreferencesField.RandomFavouriteSongsOnly, randomFavouriteSongsOnly)
        preferencesService.setValue(PreferencesField.CustomSongsGroupCategories, customSongsGroupCategories)
        preferencesService.setValue(PreferencesField.RestoreTransposition, restoreTransposition)

        preferencesService.setValue(PreferencesField.UserAuthToken, userAuthToken)

        preferencesService.saveAll()
    }

    fun reload() {
        logger.debug("reloading preferences")
        preferencesService.loadAll()
    }

}

class PreferenceDelegate<T>(
        private val field: PreferencesField
): ReadWriteProperty<PreferencesState, T> {

    override fun getValue(thisRef: PreferencesState, property: KProperty<*>): T {
        return thisRef.preferencesService.getValue(field, field.typeDef.validClass()) as T
    }

    override fun setValue(thisRef: PreferencesState, property: KProperty<*>, value: T) {
        thisRef.preferencesService.setValue(field, value)
    }

}
