package igrek.songbook.settings.preferences

import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.theme.ColorScheme
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
            defaultValue = FontTypeface.SANS_SERIF,
            serializer = FontTypeface::id,
            deserializer = FontTypeface.Companion::parseById
    )),

    ColorSchemeId(GenericLongIdPreferenceType(
            defaultValue = ColorScheme.DARK,
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

    ;

    constructor(defaultValue: String) : this(StringPreferenceType(defaultValue))

    constructor(defaultValue: Long) : this(LongPreferenceType(defaultValue))

    constructor(defaultValue: Float) : this(FloatPreferenceType(defaultValue))

    constructor(defaultValue: Boolean) : this(BooleanPreferenceType(defaultValue))

    fun preferenceName(): String {
        return this.name.decapitalize()
    }

}
