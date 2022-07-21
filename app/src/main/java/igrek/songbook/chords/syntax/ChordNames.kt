package igrek.songbook.chords.syntax

import igrek.songbook.chords.model.Note
import igrek.songbook.settings.chordsnotation.ChordsNotation


class ChordNames {
    companion object {

        val validMajorChordNames: Map<ChordsNotation, List<List<String>>> = mapOf(
            ChordsNotation.GERMAN to listOf(
                listOf("C"),
                listOf("C#", "Db", "Des", "Cis"),
                listOf("D"),
                listOf("D#", "Eb", "Es", "Dis"),
                listOf("E"),
                listOf("F", "E#", "Eis"),
                listOf("F#", "Gb", "Ges", "Fis"),
                listOf("G"),
                listOf("G#", "Ab", "As", "Gis"),
                listOf("A"),
                listOf("B", "A#", "Ais"),
                listOf("H", "Cb", "Ces"),
            ),
            ChordsNotation.GERMAN_IS to listOf(
                listOf("C"),
                listOf("Cis", "Db", "Des", "C#"),
                listOf("D"),
                listOf("Dis", "Eb", "Es", "D#"),
                listOf("E"),
                listOf("F", "E#", "Eis"),
                listOf("Fis", "Gb", "Ges", "F#"),
                listOf("G"),
                listOf("Gis", "Ab", "As", "G#"),
                listOf("A"),
                listOf("B", "A#", "Ais"),
                listOf("H", "Cb", "Ces"),
            ),
            ChordsNotation.ENGLISH to listOf(
                listOf("C"),
                listOf("C#", "Db"),
                listOf("D"),
                listOf("D#", "Eb"),
                listOf("E"),
                listOf("F", "E#"),
                listOf("F#", "Gb"),
                listOf("G"),
                listOf("G#", "Ab"),
                listOf("A"),
                listOf("Bb", "A#"),
                listOf("B", "Cb"),
            ),
            ChordsNotation.SOLFEGE to listOf(
                listOf("Do", "DO"),
                listOf("Do#", "DO#", "Reb", "REb"),
                listOf("Re", "RE"),
                listOf("Re#", "RE#", "Mib", "MIb"),
                listOf("Mi", "MI"),
                listOf("Fa", "FA", "Mi#", "MI#"),
                listOf("Fa#", "FA#", "Solb", "SOLb"),
                listOf("Sol", "SOL"),
                listOf("Sol#", "SOL#", "Lab", "LAb"),
                listOf("La", "LA"),
                listOf("Sib", "SIb", "La#", "LA#"),
                listOf("Si", "SI", "Dob", "DOb"),
            ),
        )

        val validMinorChordNames: Map<ChordsNotation, List<List<String>>> = mapOf(
            ChordsNotation.GERMAN to listOf(
                listOf("c"),
                listOf("c#", "db", "des", "cis"),
                listOf("d"),
                listOf("d#", "eb", "es", "dis"),
                listOf("e"),
                listOf("f", "e#", "eis"),
                listOf("f#", "gb", "ges", "fis"),
                listOf("g"),
                listOf("g#", "ab", "as", "gis"),
                listOf("a"),
                listOf("b", "a#", "ais"),
                listOf("h", "cb", "ces"),
            ),
            ChordsNotation.GERMAN_IS to listOf(
                listOf("c"),
                listOf("cis", "db", "des", "c#"),
                listOf("d"),
                listOf("dis", "eb", "es", "d#"),
                listOf("e"),
                listOf("f", "e#", "eis"),
                listOf("fis", "gb", "ges", "f#"),
                listOf("g"),
                listOf("gis", "ab", "as", "g#"),
                listOf("a"),
                listOf("b", "a#", "ais"),
                listOf("h", "cb", "ces"),
            ),
            ChordsNotation.ENGLISH to listOf(
                listOf("Cm"),
                listOf("C#m", "Dbm"),
                listOf("Dm"),
                listOf("D#m", "Ebm"),
                listOf("Em"),
                listOf("Fm", "E#m"),
                listOf("F#m", "Gbm"),
                listOf("Gm"),
                listOf("G#m", "Abm"),
                listOf("Am"),
                listOf("Bbm", "A#m"),
                listOf("Bm", "Cbm"),
            ),
            ChordsNotation.SOLFEGE to listOf(
                listOf("Dom", "DOm"),
                listOf("Do#m", "DO#m", "Rebm", "REbm"),
                listOf("Rem", "REm"),
                listOf("Re#m", "RE#m", "Mibm", "MIbm"),
                listOf("Mim", "MIm"),
                listOf("Fam", "FAm", "Mi#m", "MI#m"),
                listOf("Fa#m", "FA#m", "Solbm", "SOLbm"),
                listOf("Solm", "SOLm"),
                listOf("Sol#m", "SOL#m", "Labm", "LAbm"),
                listOf("Lam", "LAm"),
                listOf("Sibm", "SIbm", "La#m", "LA#m"),
                listOf("Sim", "SIm", "Dobm", "DObm"),
            ),
        )

        val sharpNotes: Map<ChordsNotation, Set<String>> = mapOf(
            ChordsNotation.GERMAN to setOf(
                "C#", "Cis", "D#", "Dis", "E#", "Eis", "F#", "Fis", "G#", "Gis", "A#", "Ais",
                "c#", "cis", "d#", "dis", "e#", "eis", "f#", "fis", "g#", "gis", "a#", "ais",
            ),
            ChordsNotation.GERMAN_IS to setOf(
                "C#", "Cis", "D#", "Dis", "E#", "Eis", "F#", "Fis", "G#", "Gis", "A#", "Ais",
                "c#", "cis", "d#", "dis", "e#", "eis", "f#", "fis", "g#", "gis", "a#", "ais",
            ),
            ChordsNotation.ENGLISH to setOf(
                "C#", "D#", "E#", "F#", "G#", "A#",
                "C#m", "D#m", "E#m", "F#m", "G#m", "A#m",
            ),
            ChordsNotation.SOLFEGE to setOf(
                "Do#", "DO#", "Re#", "RE#", "Mi#", "MI#", "Fa#", "FA#", "Sol#", "SOL#", "La#", "LA#",
                "Do#m", "DO#m", "Re#m", "RE#m", "Mi#m", "MI#m", "Fa#m", "FA#m", "Sol#m", "SOL#m", "La#m", "LA#m",
            ),
        )

        val flatNotes: Map<ChordsNotation, Set<String>> = mapOf(
            ChordsNotation.GERMAN to setOf(
                "Db", "Des", "Eb", "Es", "Gb", "Ges", "Ab", "As", "B", "Cb", "Ces",
                "db", "des", "eb", "es", "gb", "ges", "ab", "as", "b", "cb", "ces",
            ),
            ChordsNotation.GERMAN_IS to setOf(
                "Db", "Des", "Eb", "Es", "Gb", "Ges", "Ab", "As", "B", "Cb", "Ces",
                "db", "des", "eb", "es", "gb", "ges", "ab", "as", "b", "cb", "ces",
            ),
            ChordsNotation.ENGLISH to setOf(
                "Db", "Eb", "Gb", "Ab", "Bb", "Cb",
                "Dbm", "Ebm", "Gbm", "Abm", "Bbm", "Cbm",
            ),
            ChordsNotation.SOLFEGE to setOf(
                "Reb", "REb", "Mib", "MIb", "Solb", "SOLb", "Lab", "LAb", "Sib", "SIb", "Dob", "DOb",
                "Rebm", "REbm", "Mibm", "MIbm", "Solbm", "SOLbm", "Labm", "LAbm", "Sibm", "SIbm", "Dobm", "DObm",
            ),
        )

        // seems like they belong to that notation (meet rules) but they really don't
        val falseFriends: Map<ChordsNotation, Set<String>> = mapOf(
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

        fun formatNoteName(notation: ChordsNotation, note: Note, minor: Boolean): String {
            return when (notation) {
                ChordsNotation.GERMAN -> when (minor) {
                    false -> when (note) {
                        Note.C -> "C"
                        Note.C_SHARP -> "C#"
                        Note.D_FLAT -> "Db"
                        Note.D -> "D"
                        Note.D_SHARP -> "D#"
                        Note.E_FLAT -> "Eb"
                        Note.E -> "E"
                        Note.E_SHARP -> "E#"
                        Note.F -> "F"
                        Note.F_SHARP -> "F#"
                        Note.G_FLAT -> "Gb"
                        Note.G -> "G"
                        Note.G_SHARP -> "G#"
                        Note.A_FLAT -> "Ab"
                        Note.A -> "A"
                        Note.A_SHARP -> "A#"
                        Note.B_FLAT -> "B"
                        Note.B -> "H"
                        Note.C_FLAT -> "Cb"
                    }
                    true -> when (note) {
                        Note.C -> "c"
                        Note.C_SHARP -> "c#"
                        Note.D_FLAT -> "db"
                        Note.D -> "d"
                        Note.D_SHARP -> "d#"
                        Note.E_FLAT -> "eb"
                        Note.E -> "e"
                        Note.E_SHARP -> "e#"
                        Note.F -> "f"
                        Note.F_SHARP -> "f#"
                        Note.G_FLAT -> "gb"
                        Note.G -> "g"
                        Note.G_SHARP -> "g#"
                        Note.A_FLAT -> "ab"
                        Note.A -> "a"
                        Note.A_SHARP -> "a#"
                        Note.B_FLAT -> "b"
                        Note.B -> "h"
                        Note.C_FLAT -> "cb"
                    }
                }
                ChordsNotation.GERMAN_IS -> when (minor) {
                    false -> when (note) {
                        Note.C -> "C"
                        Note.C_SHARP -> "Cis"
                        Note.D_FLAT -> "Des"
                        Note.D -> "D"
                        Note.D_SHARP -> "Dis"
                        Note.E_FLAT -> "Es"
                        Note.E -> "E"
                        Note.E_SHARP -> "Eis"
                        Note.F -> "F"
                        Note.F_SHARP -> "Fis"
                        Note.G_FLAT -> "Ges"
                        Note.G -> "G"
                        Note.G_SHARP -> "Gis"
                        Note.A_FLAT -> "As"
                        Note.A -> "A"
                        Note.A_SHARP -> "Ais"
                        Note.B_FLAT -> "B"
                        Note.B -> "H"
                        Note.C_FLAT -> "Ces"
                    }
                    true -> when (note) {
                        Note.C -> "c"
                        Note.C_SHARP -> "cis"
                        Note.D_FLAT -> "des"
                        Note.D -> "d"
                        Note.D_SHARP -> "dis"
                        Note.E_FLAT -> "es"
                        Note.E -> "e"
                        Note.E_SHARP -> "eis"
                        Note.F -> "f"
                        Note.F_SHARP -> "fis"
                        Note.G_FLAT -> "ges"
                        Note.G -> "g"
                        Note.G_SHARP -> "gis"
                        Note.A_FLAT -> "as"
                        Note.A -> "a"
                        Note.A_SHARP -> "ais"
                        Note.B_FLAT -> "b"
                        Note.B -> "h"
                        Note.C_FLAT -> "ces"
                    }
                }
                ChordsNotation.ENGLISH -> {
                    val baseNote = when (note) {
                        Note.C -> "C"
                        Note.C_SHARP -> "C#"
                        Note.D_FLAT -> "Db"
                        Note.D -> "D"
                        Note.D_SHARP -> "D#"
                        Note.E_FLAT -> "Eb"
                        Note.E -> "E"
                        Note.E_SHARP -> "E#"
                        Note.F -> "F"
                        Note.F_SHARP -> "F#"
                        Note.G_FLAT -> "Gb"
                        Note.G -> "G"
                        Note.G_SHARP -> "G#"
                        Note.A_FLAT -> "Ab"
                        Note.A -> "A"
                        Note.A_SHARP -> "A#"
                        Note.B_FLAT -> "Bb"
                        Note.B -> "B"
                        Note.C_FLAT -> "Cb"
                    }
                    when (minor) {
                        false -> baseNote
                        true -> baseNote + "m"
                    }
                }
                ChordsNotation.SOLFEGE -> {
                    val baseNote = when (note) {
                        Note.C -> "Do"
                        Note.C_SHARP -> "Do#"
                        Note.D_FLAT -> "Reb"
                        Note.D -> "Re"
                        Note.D_SHARP -> "Re#"
                        Note.E_FLAT -> "Mib"
                        Note.E -> "Mi"
                        Note.E_SHARP -> "Mi#"
                        Note.F -> "Fa"
                        Note.F_SHARP -> "Fa#"
                        Note.G_FLAT -> "Solb"
                        Note.G -> "Sol"
                        Note.G_SHARP -> "Sol#"
                        Note.A_FLAT -> "Lab"
                        Note.A -> "La"
                        Note.A_SHARP -> "La#"
                        Note.B_FLAT -> "Sib"
                        Note.B -> "Si"
                        Note.C_FLAT -> "Dob"
                    }
                    when (minor) {
                        false -> baseNote
                        true -> baseNote + "m"
                    }
                }
            }
        }

    }
}