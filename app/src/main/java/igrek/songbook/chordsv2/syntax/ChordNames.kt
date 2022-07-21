package igrek.songbook.chordsv2.syntax

import igrek.songbook.settings.chordsnotation.ChordsNotation


class ChordNames {
    companion object {

        val baseNoteNames: Map<ChordsNotation, List<List<String>>> = hashMapOf(
            ChordsNotation.GERMAN to listOf(
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
                listOf("H"),
            ),
            ChordsNotation.GERMAN_IS to listOf(
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
                listOf("H"),
            ),
            ChordsNotation.ENGLISH to listOf(
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
                listOf("B"),
            ),
            ChordsNotation.SOLFEGE to listOf(
                listOf("Do", "DO"),
                listOf("Do#", "DO#", "Reb", "REb"),
                listOf("Re", "RE"),
                listOf("Re#", "RE#", "Mib", "MIb"),
                listOf("Mi", "MI"),
                listOf("Fa", "FA"),
                listOf("Fa#", "FA#", "Solb", "SOLb"),
                listOf("Sol", "SOL"),
                listOf("Sol#", "SOL#", "Lab", "LAb"),
                listOf("La", "LA"),
                listOf("Sib", "SIb", "La#", "LA#"),
                listOf("Si", "SI"),
            ),
        )

        val minorChordNames: Map<ChordsNotation, List<List<String>>> = hashMapOf(
            ChordsNotation.GERMAN to listOf(
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
                listOf("h"),
            ),
            ChordsNotation.GERMAN_IS to listOf(
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
                listOf("h"),
            ),
            ChordsNotation.ENGLISH to listOf(
                listOf("Cm"),
                listOf("C#m", "Dbm"),
                listOf("Dm"),
                listOf("D#m", "Ebm"),
                listOf("Em"),
                listOf("Fm"),
                listOf("F#m", "Gbm"),
                listOf("Gm"),
                listOf("G#m", "Abm"),
                listOf("Am"),
                listOf("Bbm", "A#m"),
                listOf("Bm"),
            ),
            ChordsNotation.SOLFEGE to listOf(
                listOf("Dom", "DOm"),
                listOf("Do#m", "DO#m", "Rebm", "REbm"),
                listOf("Rem", "REm"),
                listOf("Re#m", "RE#m", "Mibm", "MIbm"),
                listOf("Mim", "MIm"),
                listOf("Fam", "FAm"),
                listOf("Fa#m", "FA#m", "Solbm", "SOLbm"),
                listOf("Solm", "SOLm"),
                listOf("Sol#m", "SOL#m", "Labm", "LAbm"),
                listOf("Lam", "LAm"),
                listOf("Sibm", "SIbm", "La#m", "LA#m"),
                listOf("Sim", "SIm"),
            ),
        )

        // seems like they belong to that notation (meet rules) but they really don't
        val falseFriends: Map<ChordsNotation, Set<String>> = hashMapOf(
            ChordsNotation.GERMAN to setOf(
                "Cm", "C#m", "Dm", "D#m", "Em", "Fm", "F#m", "Gm", "G#m", "Am", "Bm",
                "co", "c#o", "dbo", "deso", "ciso", "do", "d#o", "ebo", "eso", "diso", "eo", "fo", "f#o", "gbo", "geso", "fiso", "go", "g#o", "abo", "aso", "giso", "ao", "bo", "a#o", "aiso", "ho",
                "cb", "c#b", "dbb", "desb", "cisb", "d#b", "ebb", "esb", "disb", "fb", "f#b", "gbb", "gesb", "fisb", "g#b", "abb", "asb", "gisb", "bb", "a#b", "aisb", "hb",
                "cm", "c#m", "dbm", "desm", "cism", "dm", "d#m", "ebm", "esm", "dism", "em", "fm", "f#m", "gbm", "gesm", "fism", "gm", "g#m", "abm", "asm", "gism", "am", "bm", "a#m", "aism", "hm",
            ),
            ChordsNotation.GERMAN_IS to setOf(
                "Cm", "Dm", "Em", "Fm", "Gm", "Am", "Bm",
                "co", "c#o", "dbo", "deso", "ciso", "do", "d#o", "ebo", "eso", "diso", "eo", "fo", "f#o", "gbo", "geso", "fiso", "go", "g#o", "abo", "aso", "giso", "ao", "bo", "a#o", "aiso", "ho",
                "cb", "c#b", "dbb", "desb", "cisb", "d#b", "ebb", "esb", "disb", "fb", "f#b", "gbb", "gesb", "fisb", "g#b", "abb", "asb", "gisb", "bb", "a#b", "aisb", "hb",
                "cm", "c#m", "dbm", "desm", "cism", "dm", "d#m", "ebm", "esm", "dism", "em", "fm", "f#m", "gbm", "gesm", "fism", "gm", "g#m", "abm", "asm", "gism", "am", "bm", "a#m", "aism", "hm",
            ),
            ChordsNotation.ENGLISH to setOf(
            ),
            ChordsNotation.SOLFEGE to setOf(
            ),
        )

    }
}