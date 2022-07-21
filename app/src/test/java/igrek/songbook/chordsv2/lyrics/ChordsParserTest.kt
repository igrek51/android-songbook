package igrek.songbook.chordsv2.lyrics

import igrek.songbook.chordsv2.model.Chord
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
        assertThat(chord.noteIndex).isEqualTo(expectedChord.originalNoteIndex)
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
        assertThat(parser.recognizeCompoundChord("C")).isNotNull
        assertThat(parser.recognizeCompoundChord("C-h")).isNotNull
        assertThat(parser.recognizeCompoundChord("C-h)")).isNotNull
        assertThat(parser.recognizeCompoundChord("(C-h)")).isNotNull
        assertThat(parser.recognizeCompoundChord("D(C-h)")).isNotNull
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
}
