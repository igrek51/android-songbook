package igrek.songbook.settings.chordsnotation

import igrek.songbook.R

enum class ChordsNotation(val id: Long, val displayNameResId: Int, val shortNameResId: Int) {

    GERMAN(1, R.string.notation_german, R.string.notation_short_german),

    GERMAN_IS(2, R.string.notation_german_is, R.string.notation_short_german_is),

    ENGLISH(3, R.string.notation_english, R.string.notation_short_english),

    SOLFEGE(4, R.string.notation_solfege, R.string.notation_short_solfege),

    ;

    companion object {
        val default: ChordsNotation
            get() = GERMAN

        fun parseById(id: Long?): ChordsNotation? {
            if (id == null)
                return null
            return values().firstOrNull { v -> v.id == id }
        }

        fun deserialize(id: Long): ChordsNotation? {
            return values().firstOrNull { v -> v.id == id }
        }
    }
}
