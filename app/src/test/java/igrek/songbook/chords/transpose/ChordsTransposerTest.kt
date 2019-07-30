package igrek.songbook.chords.transpose

import assertk.assertThat
import assertk.assertions.isEqualTo
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.junit.Test

class ChordsTransposerTest {

    private val germanTransposer = ChordsTransposer(ChordsNotation.GERMAN)
    private val germanFisTransposer = ChordsTransposer(ChordsNotation.GERMAN_IS)
    private val englishTransposer = ChordsTransposer(ChordsNotation.ENGLISH)
    private var transposed: String? = null

    @Test
    fun test_noTransposition() {
        val input = "a b c d [e f G G# B H]"
        assertThat(germanTransposer.transposeContent(input, 0)).isEqualTo(input)
        assertThat(germanTransposer.transposeContent(input, 12)).isEqualTo(input)
        assertThat(germanTransposer.transposeContent(input, -12)).isEqualTo(input)
    }

    @Test
    fun test_transposePlus1() {
        transposed = germanTransposer.transposeContent("a b c d [e f G G# B H]", 1)
        assertThat(transposed).isEqualTo("a b c d [f f# G# A H C]")
    }

    @Test
    fun test_transposeMinus1() {
        transposed = germanTransposer.transposeContent("a b c d [f f# G# A H C]", -1)
        assertThat(transposed).isEqualTo("a b c d [e f G G# B H]")
    }

    @Test
    fun test_englishChordsTranpose() {
        val input = "a b c d [e f G G# B H]"
        assertThat(englishTransposer.transposeContent(input, 0)).isEqualTo("a b c d [Em Fm G G# Bb B]")

        assertThat(englishTransposer.transposeContent(input, 1)).isEqualTo("a b c d [Fm F#m G# A B C]")
    }

    @Test
    fun test_germanFisChordsTranpose() {
        val input = "a b c d [e f G7 G# B H]"
        assertThat(germanFisTransposer.transposeContent(input, 0)).isEqualTo("a b c d [e f G7 Gis B H]")
        assertThat(germanFisTransposer.transposeContent(input, 1)).isEqualTo("a b c d [f fis Gis7 A H C]")
    }
}
