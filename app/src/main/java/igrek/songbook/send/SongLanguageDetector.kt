package igrek.songbook.send

import igrek.songbook.settings.language.SongLanguage

class SongLanguageDetector {

    private val polishCharacters = setOf(
        "ą", "ż", "ś", "ź", "ę", "ć", "ń", "ó", "ł",
        "Ą", "Ż", "Ś", "Ź", "Ę", "Ć", "Ń", "Ó", "Ł",
    )

    fun detectLanguageCode(lyrics: String): String {
        if (polishCharacters.any { it in lyrics }) {
            return SongLanguage.POLISH.langCode
        }
        return SongLanguage.ENGLISH.langCode
    }
}