package igrek.songbook.chords.model

import igrek.songbook.chords.syntax.MajorKey
import igrek.songbook.chords.syntax.MajorKey.*
import igrek.songbook.settings.chordsnotation.ChordsNotation


enum class NoteModifier {
    SHARP,
    NATURAL,
    FLAT,
}

enum class Note(val index: Int, val modifier: NoteModifier) {
    // ENGLISH notation

    C(0, NoteModifier.NATURAL),
    C_SHARP(1, NoteModifier.SHARP),
    D(2, NoteModifier.NATURAL),
    D_SHARP(3, NoteModifier.SHARP),
    E(4, NoteModifier.NATURAL),
    F(5, NoteModifier.NATURAL),
    F_SHARP(6, NoteModifier.SHARP),
    G(7, NoteModifier.NATURAL),
    G_SHARP(8, NoteModifier.SHARP),
    A(9, NoteModifier.NATURAL),
    A_SHARP(10, NoteModifier.SHARP),
    B(11, NoteModifier.NATURAL),

    D_FLAT(1, NoteModifier.FLAT),
    E_FLAT(3, NoteModifier.FLAT),
    G_FLAT(6, NoteModifier.FLAT),
    A_FLAT(8, NoteModifier.FLAT),
    B_FLAT(10, NoteModifier.FLAT),
    C_FLAT(11, NoteModifier.FLAT),
    E_SHARP(5, NoteModifier.SHARP),

}

fun indexToNote(noteIndex: Int, key: MajorKey? = null, forceModifier: NoteModifier? = null): Note {
    return when (noteIndex) {
        0 -> Note.C
        1 -> when {
            forceModifier == NoteModifier.SHARP -> Note.C_SHARP
            forceModifier == NoteModifier.FLAT -> Note.D_FLAT
            key?.isFlatty == true -> Note.D_FLAT
            else -> Note.C_SHARP
        }
        2 -> Note.D
        3 -> when {
            forceModifier == NoteModifier.SHARP -> Note.D_SHARP
            forceModifier == NoteModifier.FLAT -> Note.E_FLAT
            key?.isSharpy == true -> Note.D_SHARP
            else -> Note.E_FLAT
        }
        4 -> Note.E
        5 -> when {
            forceModifier == NoteModifier.SHARP -> Note.E_SHARP
            forceModifier == NoteModifier.NATURAL -> Note.F
            key == F_SHARP_MAJOR -> Note.E_SHARP
            else -> Note.F
        }
        6 -> when {
            forceModifier == NoteModifier.SHARP -> Note.F_SHARP
            forceModifier == NoteModifier.FLAT -> Note.G_FLAT
            key?.isFlatty == true -> Note.G_FLAT
            else -> Note.F_SHARP
        }
        7 -> Note.G
        8 -> when {
            forceModifier == NoteModifier.SHARP -> Note.G_SHARP
            forceModifier == NoteModifier.FLAT -> Note.A_FLAT
            key?.isSharpy == true -> Note.G_SHARP
            else -> Note.A_FLAT
        }
        9 -> Note.A
        10 -> when {
            forceModifier == NoteModifier.SHARP -> Note.A_SHARP
            forceModifier == NoteModifier.FLAT -> Note.B_FLAT
            key == B_MAJOR || key == F_SHARP_MAJOR -> Note.A_SHARP
            else -> Note.B_FLAT
        }
        11 -> when {
            forceModifier == NoteModifier.FLAT -> Note.C_FLAT
            forceModifier == NoteModifier.NATURAL -> Note.B
            key == F_SHARP_MAJOR -> Note.C_FLAT
            else -> Note.B
        }
        else -> throw RuntimeException("Unknown note index $noteIndex")
    }
}

fun convertToSharp(note: Note, notation: ChordsNotation): Note {
    return when(note) {
        Note.D_FLAT -> Note.C_SHARP
        Note.E_FLAT -> Note.D_SHARP
        Note.G_FLAT -> Note.F_SHARP
        Note.A_FLAT -> Note.G_SHARP
        Note.B_FLAT -> when (notation) {
            ChordsNotation.GERMAN, ChordsNotation.GERMAN_IS -> Note.B_FLAT
            else -> Note.A_SHARP
        }
        Note.C_FLAT -> Note.B
        else -> note
    }
}
