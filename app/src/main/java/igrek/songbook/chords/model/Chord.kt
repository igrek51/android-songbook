package igrek.songbook.chords.model

import igrek.songbook.chords.syntax.ChordNames
import igrek.songbook.chords.syntax.MajorKey
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
    var noteIndex: Int,
    val minor: Boolean = false,
    val suffix: String = "",
    val originalModifier: NoteModifier = NoteModifier.NATURAL,
) {

    fun format(
        notation: ChordsNotation,
        key: MajorKey? = null,
        originalModifiers: Boolean = false,
    ): String {
        val forceModifier = when {
            originalModifiers -> originalModifier
            else -> null
        }
        val note = indexToNote(noteIndex, key, forceModifier)
        val baseNote = ChordNames.formatNoteName(notation, note, minor)
        return baseNote + suffix
    }

}

// A chord composed of multiple single notes, eg. Cadd9/G
data class CompoundChord(
    val chord1: Chord,
    val splitter: String,
    val chord2: Chord,
) {
    fun format(
        notation: ChordsNotation,
        key: MajorKey? = null,
        originalModifiers: Boolean = false,
    ): String {
        val chord1String = chord1.format(notation, key, originalModifiers)
        val chord2String = chord2.format(notation, key, originalModifiers)
        return chord1String + splitter + chord2String
    }

    fun clone(): CompoundChord {
        return this.copy(
            chord1=this.chord1.copy(),
            chord2=this.chord2.copy(),
        )
    }
}
