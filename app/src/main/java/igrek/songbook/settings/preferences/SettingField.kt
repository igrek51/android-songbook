package igrek.songbook.settings.preferences

import igrek.songbook.settings.buttons.MediaButtonBehaviours
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.homescreen.HomeScreenEnum
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.DisplayStyle
import igrek.songbook.settings.theme.FontTypeface

enum class SettingField constructor(val typeDef: PreferenceTypeDefinition<*>) {

    Fontsize(20.0f), // dp

    AutoscrollInitialPause(0L), // ms

    AutoscrollSpeed(0.200f), // em / s

    ChordsNotationId(
        GenericLongIdPreferenceType(
            defaultValue = ChordsNotation.default,
            serializer = ChordsNotation::id,
            deserializer = ChordsNotation.Companion::deserialize
        )
    ),

    AppLanguage(
        GenericStringIdPreferenceType(
            defaultValue = igrek.songbook.settings.language.AppLanguage.DEFAULT,
            serializer = igrek.songbook.settings.language.AppLanguage::langCode,
            deserializer = igrek.songbook.settings.language.AppLanguage.Companion::parseByLangCode
        )
    ),

    FontTypefaceId(
        GenericStringIdPreferenceType(
            defaultValue = FontTypeface.default,
            serializer = FontTypeface::id,
            deserializer = FontTypeface.Companion::parseById
        )
    ),

    ColorSchemeId(
        GenericLongIdPreferenceType(
            defaultValue = ColorScheme.default,
            serializer = ColorScheme::id,
            deserializer = ColorScheme.Companion::parseById
        )
    ),

    ChordsEndOfLine(false),

    ChordsAbove(false),

    AutoscrollSpeedAutoAdjustment(true),

    AutoscrollSpeedVolumeKeys(true),

    RandomFavouriteSongsOnly(false),

    RandomPlaylistSongs(false),

    RestoreTransposition(true),

    ChordsInstrument(
        GenericLongIdPreferenceType(
            defaultValue = igrek.songbook.settings.enums.ChordsInstrument.default,
            serializer = igrek.songbook.settings.enums.ChordsInstrument::id,
            deserializer = igrek.songbook.settings.enums.ChordsInstrument.Companion::parseById
        )
    ),

    UserAuthToken(""),

    AppExecutionCount(0),

    AdsStatus(0),

    ChordsDisplayStyle(
        GenericLongIdPreferenceType(
            defaultValue = DisplayStyle.default,
            serializer = DisplayStyle::id,
            deserializer = DisplayStyle.Companion::parseById,
        )
    ),

    ChordsEditorFontTypeface(
        GenericStringIdPreferenceType(
            defaultValue = FontTypeface.MONOSPACE,
            serializer = FontTypeface::id,
            deserializer = FontTypeface.Companion::parseById,
        )
    ),

    KeepScreenOn(true),

    AnonymousUsageData(false),

    ChordDiagramStyle(
        GenericLongIdPreferenceType(
            defaultValue = igrek.songbook.chords.diagram.guitar.ChordDiagramStyle.default,
            serializer = igrek.songbook.chords.diagram.guitar.ChordDiagramStyle::id,
            deserializer = igrek.songbook.chords.diagram.guitar.ChordDiagramStyle.Companion::parseById,
        )
    ),

    UpdateDbOnStartup(true),

    TrimWhitespaces(true),

    AutoscrollAutostart(false),

    AutoscrollForwardNextSong(false),

    AutoscrollShowEyeFocus(true),

    AutoscrollIndividualSpeed(false),

    HorizontalScroll(false),

    MediaButtonBehaviour(
        GenericLongIdPreferenceType(
            defaultValue = MediaButtonBehaviours.default,
            serializer = MediaButtonBehaviours::id,
            deserializer = MediaButtonBehaviours.Companion::parseById,
        )
    ),

    PurchasedAdFree(false),

    HomeScreen(
        GenericLongIdPreferenceType(
            defaultValue = HomeScreenEnum.default,
            serializer = HomeScreenEnum::id,
            deserializer = HomeScreenEnum.Companion::parseById,
        )
    ),

    CustomSongsOrdering(
        GenericLongIdPreferenceType(
            defaultValue = igrek.songbook.settings.enums.CustomSongsOrdering.default,
            serializer = igrek.songbook.settings.enums.CustomSongsOrdering::id,
            deserializer = igrek.songbook.settings.enums.CustomSongsOrdering.Companion::parseById,
        )
    ),

    ForceSharpNotes(false),

    SongLyricsSearch(true),

    SyncBackupAutomatically(false),

    LastDriveBackupTimestamp(0), // in seconds

    DeviceId(""),

    LastAppVersionCode(0),

    SaveCustomSongsBackups(true),

    CastScrollControl(
        GenericLongIdPreferenceType(
            defaultValue = igrek.songbook.cast.CastScrollControl.default,
            serializer = igrek.songbook.cast.CastScrollControl::id,
            deserializer = igrek.songbook.cast.CastScrollControl.Companion::mustParseById,
        )
    ),

    SwipeToRandomizeAgain(true),

    ;

    constructor(defaultValue: String) : this(StringPreferenceType(defaultValue))
    constructor(defaultValue: Long) : this(LongPreferenceType(defaultValue))
    constructor(defaultValue: Float) : this(FloatPreferenceType(defaultValue))
    constructor(defaultValue: Boolean) : this(BooleanPreferenceType(defaultValue))

    fun preferenceName(): String {
        return this.name.replaceFirstChar { it.lowercase() }
    }
}
