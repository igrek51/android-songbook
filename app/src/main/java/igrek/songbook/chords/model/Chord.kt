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
): GeneralChord {

    override fun format(
        notation: ChordsNotation,
        key: MajorKey?,
        originalModifiers: Boolean,
        forceSharps: Boolean,
    ): String {

        val forceModifier = when {
            originalModifiers -> originalModifier
            else -> null
        }

        var note = indexToNote(noteIndex, key, forceModifier)
        if (forceSharps)
            note = convertToSharp(note, notation)

        val baseNote = ChordNames.formatNoteName(notation, note, minor)
        return baseNote + suffix
    }

    override val baseChord: Chord get() = this

    override fun clone(): Chord = this.copy()
}

// A chord composed of multiple single notes, eg. Cadd9/G
data class CompoundChord(
    val chord1: Chord,
    val splitter: String,
    val chord2: Chord,
): GeneralChord {

    override fun format(
        notation: ChordsNotation,
        key: MajorKey?,
        originalModifiers: Boolean,
        forceSharps: Boolean,
    ): String {
        val chord1String = chord1.format(notation, key, originalModifiers, forceSharps)
        val chord2String = chord2.format(notation, key, originalModifiers, forceSharps)
        return chord1String + splitter + chord2String
    }

    override val baseChord: Chord get() = this.chord1

    override fun clone(): CompoundChord {
        return this.copy(
            chord1=this.chord1.copy(),
            chord2=this.chord2.copy(),
        )
    }
}

interface GeneralChord {
    fun format(
        notation: ChordsNotation,
        key: MajorKey? = null,
        originalModifiers: Boolean = false,
        forceSharps: Boolean = false,
    ): String

    val baseChord: Chord

    fun clone(): GeneralChord
}
