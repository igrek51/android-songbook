package igrek.songbook.settings.preferences

import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.language.AppLanguage
import igrek.songbook.songpreview.theme.ColorScheme
import igrek.songbook.songpreview.theme.FontTypeface

enum class PreferencesDefinition constructor(val type: PropertyType, val defaultValue: Any?) {

    fontsize(20.0f), // dp

    autoscrollInitialPause(36000L), // ms

    autoscrollSpeed(0.15f), // em / s

    chordsNotationId(ChordsNotation.GERMAN.id),

    appLanguage(AppLanguage.DEFAULT.langCode),

    fontTypefaceId(FontTypeface.SANS_SERIF.id),

    colorSchemeId(ColorScheme.DARK.id),

    excludedLanguages(""),

    autoscrollSpeedAutoAdjustment(true),

    autoscrollSpeedVolumeKeys(true),

    randomFavouriteSongsOnly(false),

    ;

    constructor(defaultValue: String) : this(PropertyType.STRING, defaultValue)

    constructor(defaultValue: Boolean?) : this(PropertyType.BOOLEAN, defaultValue)

    constructor(defaultValue: Long?) : this(PropertyType.LONG, defaultValue)

    constructor(defaultValue: Float?) : this(PropertyType.FLOAT, defaultValue)

}
