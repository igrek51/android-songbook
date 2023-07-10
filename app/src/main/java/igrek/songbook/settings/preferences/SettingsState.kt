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

    companion object {
        val knownSettingFields: MutableMap<String, FieldDefinition<*, *>> = mutableMapOf()
    }

    var chordsNotation: ChordsNotation by SettingFieldDelegate.makeGenericLongId(
        "chordsNotationId",
        defaultValue = ChordsNotation.default,
        serializer = ChordsNotation::id,
        deserializer = ChordsNotation.Companion::deserialize
    )
    var appLanguage: AppLanguage by SettingFieldDelegate.makeGenericStringId(
        "appLanguage",
        defaultValue = AppLanguage.DEFAULT,
        serializer = AppLanguage::langCode,
        deserializer = AppLanguage.Companion::parseByLangCode
    )
    var fontTypeface: FontTypeface by SettingFieldDelegate.makeGenericStringId(
        "fontTypefaceId",
        defaultValue = FontTypeface.default,
        serializer = FontTypeface::id,
        deserializer = FontTypeface.Companion::parseById
    )
    var colorScheme: ColorScheme by SettingFieldDelegate.makeGenericLongId(
        "colorSchemeId",
        defaultValue = ColorScheme.default,
        serializer = ColorScheme::id,
        deserializer = ColorScheme.Companion::parseById
    )
    var chordsInstrument: ChordsInstrument by SettingFieldDelegate.makeGenericLongId(
        "chordsInstrument",
        defaultValue = ChordsInstrument.default,
        serializer = ChordsInstrument::id,
        deserializer = ChordsInstrument.Companion::parseById
    )
    var chordsDisplayStyle: DisplayStyle by SettingFieldDelegate.makeGenericLongId(
        "chordsDisplayStyle",
        defaultValue = DisplayStyle.default,
        serializer = DisplayStyle::id,
        deserializer = DisplayStyle.Companion::parseById,
    )
    var chordsEditorFontTypeface: FontTypeface by SettingFieldDelegate.makeGenericStringId(
        "chordsEditorFontTypeface",
        defaultValue = FontTypeface.MONOSPACE,
        serializer = FontTypeface::id,
        deserializer = FontTypeface.Companion::parseById,
    )
    var chordDiagramStyle: ChordDiagramStyle by SettingFieldDelegate.makeGenericLongId(
        "chordDiagramStyle",
        defaultValue = ChordDiagramStyle.default,
        serializer = ChordDiagramStyle::id,
        deserializer = ChordDiagramStyle.Companion::parseById,
    )
    var mediaButtonBehaviour: MediaButtonBehaviours by SettingFieldDelegate.makeGenericLongId(
        "mediaButtonBehaviour",
        defaultValue = MediaButtonBehaviours.default,
        serializer = MediaButtonBehaviours::id,
        deserializer = MediaButtonBehaviours.Companion::parseById,
    )
    var customSongsOrdering: CustomSongsOrdering by SettingFieldDelegate.makeGenericLongId(
        "customSongsOrdering",
        defaultValue = CustomSongsOrdering.default,
        serializer = CustomSongsOrdering::id,
        deserializer = CustomSongsOrdering.Companion::parseById,
    )
    var castScrollControl: CastScrollControl by SettingFieldDelegate.makeGenericLongId(
        "castScrollControl",
        defaultValue = CastScrollControl.default,
        serializer = CastScrollControl::id,
        deserializer = CastScrollControl.Companion::mustParseById,
    )
    var homeScreen: HomeScreenEnum by SettingFieldDelegate.makeGenericLongId(
        "homeScreen",
        defaultValue = HomeScreenEnum.default,
        serializer = HomeScreenEnum::id,
        deserializer = HomeScreenEnum.Companion::parseById,
    )

    var fontsize: Float by SettingFieldDelegate.make("fontsize", 20.0f) // dp
    var autoscrollSpeed: Float by SettingFieldDelegate.make("autoscrollSpeed", 0.200f) // em / s
    var autoscrollSpeedAutoAdjustment: Boolean by SettingFieldDelegate.make("autoscrollSpeedAutoAdjustment", true)
    var autoscrollSpeedVolumeKeys: Boolean by SettingFieldDelegate.make("autoscrollSpeedVolumeKeys", true)
    var randomFavouriteSongsOnly: Boolean by SettingFieldDelegate.make("randomFavouriteSongsOnly", false)
    var randomPlaylistSongs: Boolean by SettingFieldDelegate.make("randomPlaylistSongs", false)
    var restoreTransposition: Boolean by SettingFieldDelegate.make("restoreTransposition", true)
    var userAuthToken: String by SettingFieldDelegate.make("userAuthToken", "")
    var appExecutionCount: Long by SettingFieldDelegate.make("appExecutionCount", 0)
    var adsStatus: Long by SettingFieldDelegate.make("adsStatus", 0)
    var keepScreenOn: Boolean by SettingFieldDelegate.make("keepScreenOn", true)
    var anonymousUsageData: Boolean by SettingFieldDelegate.make("anonymousUsageData", false)
    var updateDbOnStartup: Boolean by SettingFieldDelegate.make("updateDbOnStartup", true)
    var trimWhitespaces: Boolean by SettingFieldDelegate.make("trimWhitespaces", true)
    var autoscrollAutostart: Boolean by SettingFieldDelegate.make("autoscrollAutostart", false)
    var autoscrollForwardNextSong: Boolean by SettingFieldDelegate.make("autoscrollForwardNextSong", false)
    var autoscrollShowEyeFocus: Boolean by SettingFieldDelegate.make("autoscrollShowEyeFocus", true)
    var autoscrollIndividualSpeed: Boolean by SettingFieldDelegate.make("autoscrollIndividualSpeed", false)
    var horizontalScroll: Boolean by SettingFieldDelegate.make("horizontalScroll", false)
    var purchasedAdFree: Boolean by SettingFieldDelegate.make("purchasedAdFree", false)
    var forceSharpNotes: Boolean by SettingFieldDelegate.make("forceSharpNotes", false)
    var songLyricsSearch: Boolean by SettingFieldDelegate.make("songLyricsSearch", true)
    var syncBackupAutomatically: Boolean by SettingFieldDelegate.make("syncBackupAutomatically", false)
    var lastDriveBackupTimestamp: Long by SettingFieldDelegate.make("lastDriveBackupTimestamp", 0) // in seconds
    var deviceId: String by SettingFieldDelegate.make("deviceId", "")
    var lastAppVersionCode: Long by SettingFieldDelegate.make("lastAppVersionCode", 0)
    var saveCustomSongsBackups: Boolean by SettingFieldDelegate.make("saveCustomSongsBackups", true)
    var swipeToRandomizeAgain: Boolean by SettingFieldDelegate.make("swipeToRandomizeAgain", true)

}
