package igrek.songbook.settings.preferences

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.instrument.ChordsInstrument
import igrek.songbook.settings.language.AppLanguage
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.FontTypeface
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PreferencesState {
    @Inject
    lateinit var preferencesService: PreferencesService

    private val logger = LoggerFactory.logger

    // preferences getters / setters proxy
    var fontsize: Float by PreferenceDelegate(PreferencesField.Fontsize)

    var appLanguage: AppLanguage by PreferenceDelegate(PreferencesField.AppLanguage)

    var chordsNotation: ChordsNotation by PreferenceDelegate(PreferencesField.ChordsNotationId)

    var fontTypeface: FontTypeface by PreferenceDelegate(PreferencesField.FontTypefaceId)

    var colorScheme: ColorScheme by PreferenceDelegate(PreferencesField.ColorSchemeId)

    var chordsEndOfLine: Boolean by PreferenceDelegate(PreferencesField.ChordsEndOfLine)

    var chordsAbove: Boolean by PreferenceDelegate(PreferencesField.ChordsAbove)

    var autoscrollInitialPause: Long by PreferenceDelegate(PreferencesField.AutoscrollInitialPause)

    var autoscrollSpeed: Float by PreferenceDelegate(PreferencesField.AutoscrollSpeed)

    var autoscrollSpeedAutoAdjustment: Boolean by PreferenceDelegate(PreferencesField.AutoscrollSpeedAutoAdjustment)

    var autoscrollSpeedVolumeKeys: Boolean by PreferenceDelegate(PreferencesField.AutoscrollSpeedVolumeKeys)

    var randomFavouriteSongsOnly: Boolean by PreferenceDelegate(PreferencesField.RandomFavouriteSongsOnly)

    var customSongsGroupCategories: Boolean by PreferenceDelegate(PreferencesField.CustomSongsGroupCategories)

    var restoreTransposition: Boolean by PreferenceDelegate(PreferencesField.RestoreTransposition)

    var chordsInstrument: ChordsInstrument by PreferenceDelegate(PreferencesField.ChordsInstrument)

    var userAuthToken: String by PreferenceDelegate(PreferencesField.UserAuthToken)

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun updateAndSave() {
        preferencesService.saveAll()
    }

    fun reload() {
        logger.debug("reloading preferences")
        preferencesService.loadAll()
    }

}

class PreferenceDelegate<T : Any>(
        private val field: PreferencesField
) : ReadWriteProperty<PreferencesState, T> {

    override fun getValue(thisRef: PreferencesState, property: KProperty<*>): T {
        return thisRef.preferencesService.getValue(field)
    }

    override fun setValue(thisRef: PreferencesState, property: KProperty<*>, value: T) {
        thisRef.preferencesService.setValue(field, value)
    }

}
