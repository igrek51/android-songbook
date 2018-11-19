package igrek.songbook.model.chords

import igrek.songbook.R

enum class ChordsNotation(val id: Long, val displayNameResId: Int) {

    GERMAN(1, R.string.notation_german),

    ENGLISH(2, R.string.notation_english);

    companion object {
        fun parseById(id: Long): ChordsNotation? {
            return ChordsNotation.values().firstOrNull { v -> v.id == id }
        }
    }
}
