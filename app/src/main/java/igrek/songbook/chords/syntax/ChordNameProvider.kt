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

    // seems like they belongs to that notation (meet rules) but they really don't
    fun falseFriends(notation: ChordsNotation): Set<String> {
        return when (notation) {
            ChordsNotation.GERMAN -> setOf(
                    "Cm", "C#m", "Dm", "D#m", "Em", "Fm", "F#m", "Gm", "G#m", "Am", "Bm",
                    "co", "c#o", "dbo", "deso", "ciso", "do", "d#o", "ebo", "eso", "diso", "eo", "fo", "f#o", "gbo", "geso", "fiso", "go", "g#o", "abo", "aso", "giso", "ao", "bo", "a#o", "aiso", "ho",
                    "cb", "c#b", "dbb", "desb", "cisb", "d#b", "ebb", "esb", "disb", "fb", "f#b", "gbb", "gesb", "fisb", "g#b", "abb", "asb", "gisb", "bb", "a#b", "aisb", "hb",
                    "cm", "c#m", "dbm", "desm", "cism", "dm", "d#m", "ebm", "esm", "dism", "em", "fm", "f#m", "gbm", "gesm", "fism", "gm", "g#m", "abm", "asm", "gism", "am", "bm", "a#m", "aism", "hm"
            )
            ChordsNotation.GERMAN_IS -> setOf(
                    "Cm", "Dm", "Em", "Fm", "Gm", "Am", "Bm",
                    "co", "c#o", "dbo", "deso", "ciso", "do", "d#o", "ebo", "eso", "diso", "eo", "fo", "f#o", "gbo", "geso", "fiso", "go", "g#o", "abo", "aso", "giso", "ao", "bo", "a#o", "aiso", "ho",
                    "cb", "c#b", "dbb", "desb", "cisb", "d#b", "ebb", "esb", "disb", "fb", "f#b", "gbb", "gesb", "fisb", "g#b", "abb", "asb", "gisb", "bb", "a#b", "aisb", "hb",
                    "cm", "c#m", "dbm", "desm", "cism", "dm", "d#m", "ebm", "esm", "dism", "em", "fm", "f#m", "gbm", "gesm", "fism", "gm", "g#m", "abm", "asm", "gism", "am", "bm", "a#m", "aism", "hm"
            )
            ChordsNotation.ENGLISH -> setOf()
        }
    }

}