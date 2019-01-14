package igrek.songbook.settings.chordsnotation

import igrek.songbook.R

enum class ChordsNotation(val id: Long, val displayNameResId: Int) {

    GERMAN(1, R.string.notation_german),

    ENGLISH(2, R.string.notation_english),

    GERMAN_IS(3, R.string.notation_german_is),

    ;

    companion object {
        fun parseById(id: Long): ChordsNotation? {
            return ChordsNotation.values().firstOrNull { v -> v.id == id }
        }
    }
}
