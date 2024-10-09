package igrek.songbook.settings.language

data class SongLanguage(val langCode: String) {

    companion object {
        const val ENGLISH_CODE: String = "en"
        const val POLISH_CODE: String = "pl"
    }
}
