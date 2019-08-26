package igrek.songbook.chords

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import igrek.songbook.chords.detector.ChordsDetector
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.junit.Test

class ChordsDetectorTest {

    @Test
    fun test_detect_fmaj7() {
        val detector = ChordsDetector(ChordsNotation.ENGLISH)
        assertThat(detector.recognizeChord("Fmaj7")).isEqualTo(Pair(17, "maj7"))
        assertThat(detector.findChords("Fmaj7")).isEqualTo("[Fmaj7]")
    }

    @Test
    fun test_detect_parentheses_Ch() {
        val detector = ChordsDetector(ChordsNotation.GERMAN)
        assertThat(detector.isWordAChord("C")).isTrue()
        assertThat(detector.isWordAChord("C-h")).isTrue()
        assertThat(detector.isWordAChord("C-h)")).isTrue()
        assertThat(detector.isWordAChord("(C-h)")).isTrue()
        assertThat(detector.isWordAChord("D(C-h)")).isTrue()
    }

}
