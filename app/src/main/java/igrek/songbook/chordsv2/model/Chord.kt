package igrek.songbook.chordsv2.model

import igrek.songbook.settings.chordsnotation.ChordsNotation

// Single chord with one single note: Cm, F#maj7, Bbm
data class Chord (
    /* Note index:
     0 - C
     1 - C# / Db
     2 - D
     3 - Eb / D#
     4 - E
     5 - F
     6 - F# / Gb
     7 - G
     8 - Ab / G#
     9 - A
     10 - Bb / A#
     11 - B
     */
    val noteIndex: Int,
    val minor: Boolean,
    val suffix: String = "",
    var displayText: String = "",
    val noteModifier: NoteModifier = NoteModifier.NATURAL,
) {

    fun render(notation: ChordsNotation, key: Int? = null): Chord {
        displayText = format(notation, key)
        return this
    }

    fun format(notation: ChordsNotation, key: Int? = null): String {
        return when (notation) {
            ChordsNotation.GERMAN -> TODO()
            ChordsNotation.GERMAN_IS -> TODO()
            ChordsNotation.ENGLISH -> TODO()
            ChordsNotation.SOLFEGE -> TODO()
        }
    }

}

enum class NoteModifier {
    SHARP,
    NATURAL,
    FLAT,
}
