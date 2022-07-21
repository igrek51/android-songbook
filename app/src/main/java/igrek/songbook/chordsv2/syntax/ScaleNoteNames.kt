package igrek.songbook.chordsv2.syntax

import igrek.songbook.chordsv2.model.Note.*
import igrek.songbook.chordsv2.model.Note

class ScaleNoteNames {
    companion object {

        val majorKeyNotes: Map<Note, List<Note>> = mapOf(
            C to listOf(C, D, E, F, G, A, B), // C Major / A minor
            G to listOf(C, D, E, F_SHARP, G, A, B), // G Major
            D to listOf(C_SHARP, D, E, F_SHARP, G, A, B), // D Major
            A to listOf(C_SHARP, D, E, F_SHARP, G_SHARP, A, B), // A Major
            E to listOf(C_SHARP, D_SHARP, E, F_SHARP, G_SHARP, A, B), // E Major
            B to listOf(C_SHARP, D_SHARP, E, F_SHARP, G_SHARP, A_SHARP, B), // B Major
            F_SHARP to listOf(C_SHARP, D_SHARP, E_SHARP, F_SHARP, G_SHARP, A_SHARP, B), // F# (Gb) Major
            G_FLAT to listOf(D_FLAT, E_FLAT, F, G_FLAT, A_FLAT, B_FLAT, C_FLAT), // Gb (F#) Major
            D_FLAT to listOf(C, D_FLAT, E_FLAT, F, G_FLAT, A_FLAT, B_FLAT), // Db (C#) Major
            A_FLAT to listOf(C, D_FLAT, E_FLAT, F, G, A_FLAT, B_FLAT), // Ab (G#) Major
            E_FLAT to listOf(C, D, E_FLAT, F, G, A_FLAT, B_FLAT), // Eb (D#) Major
            B_FLAT to listOf(C, D, E_FLAT, F, G, A, B_FLAT), // Bb (A#) Major
            F to listOf(C, D, E, F, G, A, B_FLAT), // F Major
        )

    }
}