package igrek.songbook.settings.language

enum class SongLanguage(val langCode: String) {

    UNKNOWN("_"),

    ENGLISH("en"),

    POLISH("pl");

    companion object {
        fun parseByLangCode(langCode: String): SongLanguage? {
            return SongLanguage.values().firstOrNull { v -> v.langCode == langCode }
        }
    }
}
