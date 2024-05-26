package igrek.songbook.settings.language

enum class SongLanguage(val langCode: String) {

    UNKNOWN("_"),

    ENGLISH("en"),

    POLISH("pl"),

    FRENCH("fr"),

    ;

    companion object {
        fun parseByLangCode(langCode: String): SongLanguage? {
            return entries.firstOrNull { v -> v.langCode == langCode }
        }

        fun allKnown(): Set<SongLanguage> = entries.filterNot { it == UNKNOWN }.toSet()
    }
}
