package igrek.songbook.chordsv2.model

import igrek.songbook.chordsv2.syntax.ChordNames
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

    fun render(notation: ChordsNotation, keyModifier: NoteModifier? = null): Chord {
        displayText = format(notation, keyModifier)
        return this
    }

    fun format(notation: ChordsNotation, keyModifier: NoteModifier? = null): String {
        val note = indexToNote(noteIndex, keyModifier)
        val baseNote = ChordNames.formatNoteName(notation, note, minor)
        return baseNote + suffix
    }

}
