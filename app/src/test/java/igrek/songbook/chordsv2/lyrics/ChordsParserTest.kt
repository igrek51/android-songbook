package igrek.songbook.chordsv2.lyrics

import igrek.songbook.chordsv2.model.Chord
import igrek.songbook.chordsv2.model.ChordFragment
import igrek.songbook.chordsv2.model.ChordFragmentType
import igrek.songbook.chordsv2.model.CompoundChord
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class ChordsParserTest {

    @Test
    fun test_recognize_major() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertRecognizeSingleChord(parser, "C", Chord(0, false, ""))
        assertRecognizeSingleChord(parser, "C#", Chord(1, false, ""))
        assertRecognizeSingleChord(parser, "D", Chord(2, false, ""))
        assertThat(parser.recognizeSingleChord("C#add9")).isNotNull

        assertRecognizeSingleChord(parser, "Csus4", Chord(0, false, "sus4"))
        assertRecognizeSingleChord(parser, "C#sus4", Chord(1, false, "sus4"))
        assertRecognizeSingleChord(parser, "Dsus4", Chord(2, false, "sus4"))

        assertNull(parser.recognizeSingleChord("dupa"))
    }

    private fun assertRecognizeSingleChord(parser: ChordParser, name: String, expectedChord: Chord) {
        val chord = parser.recognizeSingleChord(name)
        assertNotNull(chord)
        assertThat(chord.noteIndex).isEqualTo(expectedChord.noteIndex)
        assertThat(chord.minor).isEqualTo(expectedChord.minor)
        assertThat(chord.suffix).isEqualTo(expectedChord.suffix)
    }

    @Test
    fun test_recognize_minor() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertRecognizeSingleChord(parser, "c", Chord(0, true, ""))
        assertRecognizeSingleChord(parser, "c#", Chord(1, true, ""))
        assertRecognizeSingleChord(parser, "d", Chord(2, true, ""))

        assertRecognizeSingleChord(parser, "csus4", Chord(0, true, "sus4"))
        assertRecognizeSingleChord(parser, "c#sus4", Chord(1, true, "sus4"))
        assertRecognizeSingleChord(parser, "dsus4", Chord(2, true, "sus4"))

        assertRecognizeSingleChord(parser, "dis", Chord(3, true, ""))
        assertRecognizeSingleChord(parser, "es", Chord(3, true, ""))

        assertNull(parser.recognizeSingleChord("dupa"))
    }

    @Test
    fun test_detect_fmaj7() {
        val parser = ChordParser(ChordsNotation.ENGLISH)
        assertRecognizeSingleChord(parser, "Fmaj7", Chord(5, false, "maj7"))
        assertRecognizeSingleChord(parser, "Fm", Chord(5, true, ""))
    }

    @Test
    fun test_c_plus() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertRecognizeSingleChord(parser, "C+", Chord(0, false, "+"))
        assertThat(parser.recognizeSingleChord("C+")).isNotNull
    }

    @Test
    fun test_c_minus() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertRecognizeSingleChord(parser, "C-", Chord(0, false, "-"))
        assertThat(parser.recognizeSingleChord("C-")).isNotNull
    }

    @Test
    fun test_detect_parentheses_Ch() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeCompoundChord("C")).isNull()
        assertThat(parser.recognizeSingleChord("C")).isEqualTo(Chord(0, false, "", "C"))
        assertThat(parser.recognizeCompoundChord("C-h")).isEqualTo(CompoundChord("C-h", Chord(0, false, "", "C"), "-", Chord(11, true, "", "h")))
        assertThat(parser.recognizeCompoundChord("(C-h)")).isNull()

        val unknowns = mutableSetOf<String>()
        val fragments = parser.parseChordFragments("Dmaj7(C-h)", unknowns)

        assertThat(unknowns).isEmpty()
        assertThat(fragments).hasSize(6)
        assertThat(fragments[0]).isEqualTo(ChordFragment("Dmaj7", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(2, false, "maj7", "Dmaj7")))
        assertThat(fragments[1]).isEqualTo(ChordFragment("(", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[2]).isEqualTo(ChordFragment("C", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(0, false, "", "C")))
        assertThat(fragments[3]).isEqualTo(ChordFragment("-", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[4]).isEqualTo(ChordFragment("h", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(11, true, "", "h")))
        assertThat(fragments[5]).isEqualTo(ChordFragment(")", ChordFragmentType.CHORD_SPLITTER))

    }

    @Test
    fun test_not_a_chord() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("Dupa")).isNull()
        assertThat(parser.recognizeSingleChord("Cmaj7blahblahblah")).isNull()
        assertThat(parser.recognizeSingleChord("Dsus888")).isNull()
        assertThat(parser.recognizeSingleChord(" ")).isNull()
        assertThat(parser.recognizeSingleChord("")).isNull()
        val parser2 = ChordParser(ChordsNotation.ENGLISH)
        assertThat(parser2.recognizeSingleChord("Dmupa")).isNull()
    }

    @Test
    fun test_suffix() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("Fmaj7")).isNotNull
        assertThat(parser.recognizeSingleChord("G#maj7-F")).isNull()
        assertThat(parser.recognizeCompoundChord("G#maj7-F")).isNotNull
    }

    @Test
    fun test_minor_chords() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("cmaj7")).isNotNull
    }

    @Test
    fun test_cis() {
        val parser = ChordParser(ChordsNotation.GERMAN_IS)
        assertThat(parser.recognizeSingleChord("cismaj7")).isNotNull
    }

    @Test
    fun test_detect_english_minor() {
        val parser = ChordParser(ChordsNotation.ENGLISH)
        assertThat(parser.recognizeSingleChord("Cm")).isNotNull
        assertRecognizeSingleChord(parser, "Dm", Chord(2, true, ""))
        assertRecognizeSingleChord(parser, "Dmmaj7", Chord(2, true, "maj7"))
        assertRecognizeSingleChord(parser, "Dmaj7", Chord(2, false, "maj7"))
        assertRecognizeSingleChord(parser, "D", Chord(2, false, ""))
    }

    @Test
    fun test_slashed_chord() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeCompoundChord("C/H")).isNotNull
        assertThat(parser.recognizeCompoundChord("C/Y")).isNull()
        assertThat(parser.recognizeCompoundChord("a/G")).isNotNull
    }

    @Test
    fun test_g6add11() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("G6add11")).isNotNull
        assertRecognizeSingleChord(parser, "G6add11",
            Chord(
                noteIndex = 7,
                minor = false,
                suffix = "6add11"
            )
        )
    }

    @Test
    fun test_false_friends() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("Am")).isNull()
        assertThat(parser.recognizeSingleChord("co")).isNull()
    }

    @Test
    fun test_detect_solfege() {
        val parser = ChordParser(ChordsNotation.SOLFEGE)
        assertRecognizeSingleChord(parser, "Do", Chord(0, false, ""))
        assertRecognizeSingleChord(parser, "Dom", Chord(0, true, ""))
        assertRecognizeSingleChord(parser, "DOm", Chord(0, true, ""))
        assertRecognizeSingleChord(parser, "Do#m", Chord(1, true, ""))
        assertRecognizeSingleChord(parser, "Re", Chord(2, false, ""))
        assertRecognizeSingleChord(parser, "Mibmaj7", Chord(3, false, "maj7"))
    }

    @Test
    fun test_parseChordFragments() {
        val parser = ChordParser(ChordsNotation.ENGLISH)
        val unknowns = mutableSetOf<String>()

        val fragments = parser.parseChordFragments("Cmaj7/G# (D-D/E)   dupa", unknowns)

        assertThat(unknowns).contains("dupa")
        assertThat(fragments).hasSize(9)
        assertThat(fragments[0]).isEqualTo(ChordFragment("Cmaj7/G#", ChordFragmentType.COMPOUND_CHORD, compoundChord = CompoundChord(
            "Cmaj7/G#", Chord(0, false, "maj7", "Cmaj7"), "/", Chord(8, false, "", "G#")
        )))
        assertThat(fragments[1]).isEqualTo(ChordFragment(" (", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[2]).isEqualTo(ChordFragment("D", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(2, false, "", "D")))
        assertThat(fragments[3]).isEqualTo(ChordFragment("-", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[4]).isEqualTo(ChordFragment("D", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(2, false, "", "D")))
        assertThat(fragments[5]).isEqualTo(ChordFragment("/", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[6]).isEqualTo(ChordFragment("E", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(4, false, "", "E")))
        assertThat(fragments[7]).isEqualTo(ChordFragment(")   ", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[8]).isEqualTo(ChordFragment("dupa", ChordFragmentType.UNKNOWN_CHORD))

    }
}
