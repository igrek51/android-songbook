package igrek.songbook.chords.syntax

import igrek.songbook.settings.chordsnotation.ChordsNotation

class ChordNameProvider {

    fun baseNotesNames(notation: ChordsNotation): List<List<String>> {
        return when (notation) {
            ChordsNotation.GERMAN -> listOf(
                    listOf("C"),
                    listOf("C#", "Db", "Des", "Cis"),
                    listOf("D"),
                    listOf("D#", "Eb", "Es", "Dis"),
                    listOf("E"),
                    listOf("F"),
                    listOf("F#", "Gb", "Ges", "Fis"),
                    listOf("G"),
                    listOf("G#", "Ab", "As", "Gis"),
                    listOf("A"),
                    listOf("B", "A#", "Ais"),
                    listOf("H")
            )
            ChordsNotation.GERMAN_IS -> listOf(
                    listOf("C"),
                    listOf("Cis", "Db", "Des", "C#"),
                    listOf("D"),
                    listOf("Dis", "Eb", "Es", "D#"),
                    listOf("E"),
                    listOf("F"),
                    listOf("Fis", "Gb", "Ges", "F#"),
                    listOf("G"),
                    listOf("Gis", "Ab", "As", "G#"),
                    listOf("A"),
                    listOf("B", "A#", "Ais"),
                    listOf("H")
            )
            ChordsNotation.ENGLISH -> listOf(
                    listOf("C"),
                    listOf("C#", "Db"),
                    listOf("D"),
                    listOf("D#", "Eb"),
                    listOf("E"),
                    listOf("F"),
                    listOf("F#", "Gb"),
                    listOf("G"),
                    listOf("G#", "Ab"),
                    listOf("A"),
                    listOf("Bb", "A#"),
                    listOf("B")
            )
        }
    }

    fun minorChords(notation: ChordsNotation): List<List<String>> {
        return when (notation) {
            ChordsNotation.GERMAN -> listOf(
                    listOf("c"),
                    listOf("c#", "db", "des", "cis"),
                    listOf("d"),
                    listOf("d#", "eb", "es", "dis"),
                    listOf("e"),
                    listOf("f"),
                    listOf("f#", "gb", "ges", "fis"),
                    listOf("g"),
                    listOf("g#", "ab", "as", "gis"),
                    listOf("a"),
                    listOf("b", "a#", "ais"),
                    listOf("h")
            )
            ChordsNotation.GERMAN_IS -> listOf(
                    listOf("c"),
                    listOf("cis", "db", "des", "c#"),
                    listOf("d"),
                    listOf("dis", "eb", "es", "d#"),
                    listOf("e"),
                    listOf("f"),
                    listOf("fis", "gb", "ges", "f#"),
                    listOf("g"),
                    listOf("gis", "ab", "as", "g#"),
                    listOf("a"),
                    listOf("b", "a#", "ais"),
                    listOf("h")
            )
            ChordsNotation.ENGLISH -> listOf(
                    listOf("Cm"),
                    listOf("C#m"),
                    listOf("Dm"),
                    listOf("D#m"),
                    listOf("Em"),
                    listOf("Fm"),
                    listOf("F#m"),
                    listOf("Gm"),
                    listOf("G#m"),
                    listOf("Am"),
                    listOf("Bbm"),
                    listOf("Bm")
            )
        }
    }

}