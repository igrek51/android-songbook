package igrek.songbook.settings.preferences

import igrek.songbook.chords.diagram.ChordDiagramStyle
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.instrument.ChordsInstrument
import igrek.songbook.settings.language.AppLanguage
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.DisplayStyle
import igrek.songbook.settings.theme.FontTypeface
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PreferencesState(
        preferencesService: LazyInject<PreferencesService> = appFactory.preferencesService,
) {
    internal val preferencesService by LazyExtractor(preferencesService)

    var fontsize: Float by PreferenceDelegate(PreferencesField.Fontsize)
    var appLanguage: AppLanguage by PreferenceDelegate(PreferencesField.AppLanguage)
    var chordsNotation: ChordsNotation by PreferenceDelegate(PreferencesField.ChordsNotationId)
    var fontTypeface: FontTypeface by PreferenceDelegate(PreferencesField.FontTypefaceId)
    var colorScheme: ColorScheme by PreferenceDelegate(PreferencesField.ColorSchemeId)
    var autoscrollInitialPause: Long by PreferenceDelegate(PreferencesField.AutoscrollInitialPause)
    var autoscrollSpeed: Float by PreferenceDelegate(PreferencesField.AutoscrollSpeed)
    var autoscrollSpeedAutoAdjustment: Boolean by PreferenceDelegate(PreferencesField.AutoscrollSpeedAutoAdjustment)
    var autoscrollSpeedVolumeKeys: Boolean by PreferenceDelegate(PreferencesField.AutoscrollSpeedVolumeKeys)
    var randomFavouriteSongsOnly: Boolean by PreferenceDelegate(PreferencesField.RandomFavouriteSongsOnly)
    var customSongsGroupCategories: Boolean by PreferenceDelegate(PreferencesField.CustomSongsGroupCategories)
    var restoreTransposition: Boolean by PreferenceDelegate(PreferencesField.RestoreTransposition)
    var chordsInstrument: ChordsInstrument by PreferenceDelegate(PreferencesField.ChordsInstrument)
    var userAuthToken: String by PreferenceDelegate(PreferencesField.UserAuthToken)
    var appExecutionCount: Long by PreferenceDelegate(PreferencesField.AppExecutionCount)
    var adsStatus: Long by PreferenceDelegate(PreferencesField.AdsStatus)
    var chordsDisplayStyle: DisplayStyle by PreferenceDelegate(PreferencesField.ChordsDisplayStyle)
    var chordsEditorFontTypeface: FontTypeface by PreferenceDelegate(PreferencesField.ChordsEditorFontTypeface)
    var keepScreenOn: Boolean by PreferenceDelegate(PreferencesField.KeepScreenOn)
    var anonymousUsageData: Boolean by PreferenceDelegate(PreferencesField.AnonymousUsageData)
    var chordDiagramStyle: ChordDiagramStyle by PreferenceDelegate(PreferencesField.ChordDiagramStyle)

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
