package igrek.songbook.settings.preferences

import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.FontTypeface

enum class PreferencesDefinition constructor(val type: PropertyType, val defaultValue: Any?) {

    Fontsize(20.0f), // dp

    AutoscrollInitialPause(36000L), // ms

    AutoscrollSpeed(0.15f), // em / s

    ChordsNotationId(ChordsNotation.GERMAN.id),

    AppLanguage(igrek.songbook.settings.language.AppLanguage.DEFAULT.langCode),

    FontTypefaceId(FontTypeface.SANS_SERIF.id),

    ColorSchemeId(ColorScheme.DARK.id),

    ChordsEndOfLine(false),

    AutoscrollSpeedAutoAdjustment(true),

    AutoscrollSpeedVolumeKeys(true),

    RandomFavouriteSongsOnly(false),

    CustomSongsGroupCategories(false),

    RestoreTransposition(true),

    ;

    constructor(defaultValue: String) : this(PropertyType.STRING, defaultValue)

    constructor(defaultValue: Boolean?) : this(PropertyType.BOOLEAN, defaultValue)

    constructor(defaultValue: Long?) : this(PropertyType.LONG, defaultValue)

    constructor(defaultValue: Float?) : this(PropertyType.FLOAT, defaultValue)

    fun preferenceName(): String {
        return this.name.decapitalize()
    }

}
