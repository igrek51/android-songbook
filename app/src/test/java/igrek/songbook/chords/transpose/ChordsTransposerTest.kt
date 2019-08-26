package igrek.songbook.chords.transpose

import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ChordsTransposerTest {

    private val germanTransposer = ChordsTransposer(fromNotation = ChordsNotation.default, toNotation = ChordsNotation.GERMAN)
    private val germanFisTransposer = ChordsTransposer(fromNotation = ChordsNotation.default, toNotation = ChordsNotation.GERMAN_IS)
    private val englishTransposer = ChordsTransposer(fromNotation = ChordsNotation.default, toNotation = ChordsNotation.ENGLISH)
    private var transposed: String? = null

    @Test
    fun test_noTransposition() {
        val input = "a b c d [e f G G# B H]"
        assertThat(germanTransposer.transposeLyrics(input, 0)).isEqualTo(input)
        assertThat(germanTransposer.transposeLyrics(input, 12)).isEqualTo(input)
        assertThat(germanTransposer.transposeLyrics(input, -12)).isEqualTo(input)
    }

    @Test
    fun test_transposePlus1() {
        transposed = germanTransposer.transposeLyrics("a b c d [e f G G# B H]", 1)
        assertThat(transposed).isEqualTo("a b c d [f f# G# A H C]")
    }

    @Test
    fun test_transposeMinus1() {
        transposed = germanTransposer.transposeLyrics("a b c d [f f# G# A H C]", -1)
        assertThat(transposed).isEqualTo("a b c d [e f G G# B H]")
    }

    @Test
    fun test_englishChordsTranpose() {
        val input = "a b c d [e f G G# B H]"
        assertThat(englishTransposer.transposeLyrics(input, 0)).isEqualTo("a b c d [Em Fm G G# Bb B]")

        assertThat(englishTransposer.transposeLyrics(input, 1)).isEqualTo("a b c d [Fm F#m G# A B C]")
    }

    @Test
    fun test_germanFisChordsTranpose() {
        val input = "a b c d [e f G7 G# B H]"
        assertThat(germanFisTransposer.transposeLyrics(input, 0)).isEqualTo("a b c d [e f G7 Gis B H]")
        assertThat(germanFisTransposer.transposeLyrics(input, 1)).isEqualTo("a b c d [f fis Gis7 A H C]")
    }

    @Test
    fun test_parentheses_Ch() {
        transposed = germanTransposer.transposeLyrics("[(C-h)]", 1)
        assertThat(transposed).isEqualTo("[(C#-c)]")
    }

    @Test
    fun test_transposeGermanChords() {
        assertThat(germanTransposer.transposeChord("a", 0)).isEqualTo("a")
        assertThat(germanTransposer.transposeChord("a", 1)).isEqualTo("b")
        assertThat(germanTransposer.transposeChord("A", 1)).isEqualTo("B")
        assertThat(germanTransposer.transposeChord("A", 2)).isEqualTo("H")
        assertThat(germanTransposer.transposeChord("A", 3)).isEqualTo("C")
        assertThat(germanTransposer.transposeChord("A", 4)).isEqualTo("C#")
        assertThat(germanTransposer.transposeChord("not_a_chord", 4)).isEqualTo("not_a_chord")
    }

    @Test
    fun test_transposeLyrics_German() {
        assertThat(germanTransposer.transposeLyrics("[a]", 0)).isEqualTo("[a]")
        assertThat(germanTransposer.transposeLyrics("[a]", 1)).isEqualTo("[b]")
        assertThat(germanTransposer.transposeLyrics("[a]", 3)).isEqualTo("[c]")
        assertThat(germanTransposer.transposeLyrics("[A]", 3)).isEqualTo("[C]")
    }

    @Test
    fun test_splitting_chords() {
        assertThat(germanTransposer.transposeLyrics("[a-F-C\na-C/G]", 2)).isEqualTo("[h-G-D\nh-D/A]")
    }

    @Test
    fun test_transposing_with_newlines() {
        assertThat(germanTransposer.transposeLyrics("[a\nF\n]", 2)).isEqualTo("[h\nG\n]")
    }

    @Test
    fun test_convert_german_moll_to_english() {
        assertThat(englishTransposer.transposeLyrics("[e]", 0)).isEqualTo("[Em]")
    }
}
