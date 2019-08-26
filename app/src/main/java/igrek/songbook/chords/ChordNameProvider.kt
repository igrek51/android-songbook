package igrek.songbook.chords

import igrek.songbook.settings.chordsnotation.ChordsNotation

class ChordNameProvider {

    fun minorChords(notation: ChordsNotation): List<String> {
        return when (notation) {
            ChordsNotation.GERMAN -> listOf(
                    "c", "c#", "d", "d#", "e", "f", "f#", "g", "g#", "a", "b", "h"
            )
            ChordsNotation.GERMAN_IS -> listOf(
                    "c", "cis", "d", "dis", "e", "f", "fis", "g", "gis", "a", "b", "h"
            )
            ChordsNotation.ENGLISH -> listOf(
                    "Cm", "C#m", "Dm", "D#m", "Em", "Fm", "F#m", "Gm", "G#m", "Am", "Bbm", "Bm"
            )
        }
    }

    fun majorChords(notation: ChordsNotation): List<String> {
        return when (notation) {
            ChordsNotation.GERMAN -> listOf(
                    "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "B", "H"
            )
            ChordsNotation.GERMAN_IS -> listOf(
                    "C", "Cis", "D", "Dis", "E", "F", "Fis", "G", "Gis", "A", "B", "H"
            )
            ChordsNotation.ENGLISH -> listOf(
                    "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "Bb", "B"
            )
        }
    }

    fun exceptionChords(notation: ChordsNotation?): Map<String, String> {
        return when (notation) {
            ChordsNotation.GERMAN, ChordsNotation.GERMAN_IS -> mapOf(
                    "Cmaj" to "C",
                    "Dmaj" to "D",
                    "Emaj" to "E",
                    "Fmaj" to "F",
                    "Gmaj" to "G",
                    "Amaj" to "A",
                    "Hmaj" to "H",
                    "Bmaj" to "B"
            )
            ChordsNotation.ENGLISH -> mapOf(
                    "Cmaj" to "C",
                    "Dmaj" to "D",
                    "Emaj" to "E",
                    "Fmaj" to "F",
                    "Gmaj" to "G",
                    "Amaj" to "A",
                    "Bbmaj" to "Bb",
                    "Bmaj" to "B"
            )
            null -> exceptionChords(ChordsNotation.ENGLISH)
        }
    }

    fun allChords(notation: ChordsNotation?): List<String> {
        if (notation == null)
            return allChords()
        return minorChords(notation)
                .plus(majorChords(notation))
    }

    fun allChords(): List<String> {
        return ChordsNotation.values().map { notation -> allChords(notation) }.flatten()
    }

}