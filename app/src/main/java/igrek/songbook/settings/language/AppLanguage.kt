package igrek.songbook.settings.language

import igrek.songbook.R

enum class AppLanguage(val langCode: String, val displayNameResId: Int) {

    DEFAULT("", R.string.language_default),

    ENGLISH("en", R.string.language_english),

    POLISH("pl", R.string.language_polish);

    companion object {
        fun parseByLangCode(langCode: String): AppLanguage? {
            return values().firstOrNull { v -> v.langCode == langCode }
        }
    }
}
