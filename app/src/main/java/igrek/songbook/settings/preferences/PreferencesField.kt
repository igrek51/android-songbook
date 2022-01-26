package igrek.songbook.settings.preferences

import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.DisplayStyle
import igrek.songbook.settings.theme.FontTypeface

enum class PreferencesField constructor(
        val typeDef: PreferenceTypeDefinition<*>
) {

    Fontsize(20.0f), // dp

    AutoscrollInitialPause(36000L), // ms

    AutoscrollSpeed(0.15f), // em / s

    ChordsNotationId(GenericLongIdPreferenceType(
            defaultValue = ChordsNotation.default,
            serializer = ChordsNotation::id,
            deserializer = ChordsNotation.Companion::deserialize
    )),

    AppLanguage(GenericStringIdPreferenceType(
            defaultValue = igrek.songbook.settings.language.AppLanguage.DEFAULT,
            serializer = igrek.songbook.settings.language.AppLanguage::langCode,
            deserializer = igrek.songbook.settings.language.AppLanguage.Companion::parseByLangCode
    )),

    FontTypefaceId(GenericStringIdPreferenceType(
            defaultValue = FontTypeface.default,
            serializer = FontTypeface::id,
            deserializer = FontTypeface.Companion::parseById
    )),

    ColorSchemeId(GenericLongIdPreferenceType(
            defaultValue = ColorScheme.default,
            serializer = ColorScheme::id,
            deserializer = ColorScheme.Companion::parseById
    )),

    ChordsEndOfLine(false),

    ChordsAbove(false),

    AutoscrollSpeedAutoAdjustment(true),

    AutoscrollSpeedVolumeKeys(true),

    RandomFavouriteSongsOnly(false),

    CustomSongsGroupCategories(false),

    RestoreTransposition(true),

    ChordsInstrument(GenericLongIdPreferenceType(
            defaultValue = igrek.songbook.settings.instrument.ChordsInstrument.default,
            serializer = igrek.songbook.settings.instrument.ChordsInstrument::id,
            deserializer = igrek.songbook.settings.instrument.ChordsInstrument.Companion::parseById
    )),

    UserAuthToken(""),

    AppExecutionCount(0),

    AdsStatus(0),

    ChordsDisplayStyle(GenericLongIdPreferenceType(
            defaultValue = DisplayStyle.default,
            serializer = DisplayStyle::id,
            deserializer = DisplayStyle.Companion::parseById,
    )),

    ChordsEditorFontTypeface(GenericStringIdPreferenceType(
            defaultValue = FontTypeface.MONOSPACE,
            serializer = FontTypeface::id,
            deserializer = FontTypeface.Companion::parseById,
    )),

    KeepScreenOn(true),

    AnonymousUsageData(false),

    ChordDiagramStyle(
        GenericLongIdPreferenceType(
            defaultValue = igrek.songbook.chords.diagram.ChordDiagramStyle.default,
            serializer = igrek.songbook.chords.diagram.ChordDiagramStyle::id,
            deserializer = igrek.songbook.chords.diagram.ChordDiagramStyle.Companion::parseById,
        )
    ),

    UpdateDbOnStartup(true),

    AutoscrollForwardNextSong(false),

    ;

    constructor(defaultValue: String) : this(StringPreferenceType(defaultValue))

    constructor(defaultValue: Long) : this(LongPreferenceType(defaultValue))

    constructor(defaultValue: Float) : this(FloatPreferenceType(defaultValue))

    constructor(defaultValue: Boolean) : this(BooleanPreferenceType(defaultValue))

    fun preferenceName(): String {
        return this.name.replaceFirstChar { it.lowercase() }
    }

}
