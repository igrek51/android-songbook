package igrek.songbook.settings.preferences

import igrek.songbook.cast.CastScrollControl
import igrek.songbook.chords.diagram.guitar.ChordDiagramStyle
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.buttons.MediaButtonBehaviours
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.enums.ChordsInstrument
import igrek.songbook.settings.enums.CustomSongsOrdering
import igrek.songbook.settings.homescreen.HomeScreenEnum
import igrek.songbook.settings.language.AppLanguage
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.DisplayStyle
import igrek.songbook.settings.theme.FontTypeface

class SettingsState(
    preferencesService: LazyInject<PreferencesService> = appFactory.preferencesService,
) {
    internal val preferencesService by LazyExtractor(preferencesService)

    var fontsize: Float by PreferenceDelegate(SettingField.Fontsize)
    var appLanguage: AppLanguage by PreferenceDelegate(SettingField.AppLanguage)
    var chordsNotation: ChordsNotation by PreferenceDelegate(SettingField.ChordsNotationId)
    var fontTypeface: FontTypeface by PreferenceDelegate(SettingField.FontTypefaceId)
    var colorScheme: ColorScheme by PreferenceDelegate(SettingField.ColorSchemeId)
    var autoscrollSpeed: Float by PreferenceDelegate(SettingField.AutoscrollSpeed)
    var autoscrollSpeedAutoAdjustment: Boolean by PreferenceDelegate(SettingField.AutoscrollSpeedAutoAdjustment)
    var autoscrollSpeedVolumeKeys: Boolean by PreferenceDelegate(SettingField.AutoscrollSpeedVolumeKeys)
    var randomFavouriteSongsOnly: Boolean by PreferenceDelegate(SettingField.RandomFavouriteSongsOnly)
    var randomPlaylistSongs: Boolean by PreferenceDelegate(SettingField.RandomPlaylistSongs)
    var restoreTransposition: Boolean by PreferenceDelegate(SettingField.RestoreTransposition)
    var chordsInstrument: ChordsInstrument by PreferenceDelegate(SettingField.ChordsInstrument)
    var userAuthToken: String by PreferenceDelegate(SettingField.UserAuthToken)
    var appExecutionCount: Long by PreferenceDelegate(SettingField.AppExecutionCount)
    var adsStatus: Long by PreferenceDelegate(SettingField.AdsStatus)
    var chordsDisplayStyle: DisplayStyle by PreferenceDelegate(SettingField.ChordsDisplayStyle)
    var chordsEditorFontTypeface: FontTypeface by PreferenceDelegate(SettingField.ChordsEditorFontTypeface)
    var keepScreenOn: Boolean by PreferenceDelegate(SettingField.KeepScreenOn)
    var anonymousUsageData: Boolean by PreferenceDelegate(SettingField.AnonymousUsageData)
    var chordDiagramStyle: ChordDiagramStyle by PreferenceDelegate(SettingField.ChordDiagramStyle)
    var updateDbOnStartup: Boolean by PreferenceDelegate(SettingField.UpdateDbOnStartup)
    var trimWhitespaces: Boolean by PreferenceDelegate(SettingField.TrimWhitespaces)
    var autoscrollAutostart: Boolean by PreferenceDelegate(SettingField.AutoscrollAutostart)
    var autoscrollForwardNextSong: Boolean by PreferenceDelegate(SettingField.AutoscrollForwardNextSong)
    var autoscrollShowEyeFocus: Boolean by PreferenceDelegate(SettingField.AutoscrollShowEyeFocus)
    var autoscrollIndividualSpeed: Boolean by PreferenceDelegate(SettingField.AutoscrollIndividualSpeed)
    var horizontalScroll: Boolean by PreferenceDelegate(SettingField.HorizontalScroll)
    var mediaButtonBehaviour: MediaButtonBehaviours by PreferenceDelegate(SettingField.MediaButtonBehaviour)
    var purchasedAdFree: Boolean by PreferenceDelegate(SettingField.PurchasedAdFree)
    var homeScreen: HomeScreenEnum by PreferenceDelegate(SettingField.HomeScreen)
    var forceSharpNotes: Boolean by PreferenceDelegate(SettingField.ForceSharpNotes)
    var customSongsOrdering: CustomSongsOrdering by PreferenceDelegate(SettingField.CustomSongsOrdering)
    var songLyricsSearch: Boolean by PreferenceDelegate(SettingField.SongLyricsSearch)
    var syncBackupAutomatically: Boolean by PreferenceDelegate(SettingField.SyncBackupAutomatically)
    var lastDriveBackupTimestamp: Long by PreferenceDelegate(SettingField.LastDriveBackupTimestamp)
    var deviceId: String by PreferenceDelegate(SettingField.DeviceId)
    var lastAppVersionCode: Long by PreferenceDelegate(SettingField.LastAppVersionCode)
    var saveCustomSongsBackups: Boolean by PreferenceDelegate(SettingField.SaveCustomSongsBackups)
    var castScrollControl: CastScrollControl by PreferenceDelegate(SettingField.CastScrollControl)
    var swipeToRandomizeAgain: Boolean by PreferenceDelegate(SettingField.SwipeToRandomizeAgain)

}