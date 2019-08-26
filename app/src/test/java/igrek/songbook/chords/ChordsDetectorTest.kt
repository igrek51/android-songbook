package igrek.songbook.chords

import org.assertj.core.api.Assertions.assertThat
import igrek.songbook.chords.detector.Chord
import igrek.songbook.chords.detector.ChordsDetector
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.junit.Test

class ChordsDetectorTest {

    @Test
    fun test_recognize_major() {
        val detector = ChordsDetector(ChordsNotation.GERMAN)
        assertThat(detector.recognizeChord("C")).isEqualTo(Chord(0, false, ""))
        assertThat(detector.recognizeChord("C#")).isEqualTo(Chord(1, false, ""))
        assertThat(detector.recognizeChord("D")).isEqualTo(Chord(2, false, ""))

        assertThat(detector.recognizeChord("Csus4")).isEqualTo(Chord(0, false, "sus4"))
        assertThat(detector.recognizeChord("C#sus4")).isEqualTo(Chord(1, false, "sus4"))
        assertThat(detector.recognizeChord("Dsus4")).isEqualTo(Chord(2, false, "sus4"))
    }

    @Test
    fun test_recognize_minor() {
        val detector = ChordsDetector(ChordsNotation.GERMAN)
        assertThat(detector.recognizeChord("c")).isEqualTo(Chord(0, true, ""))
        assertThat(detector.recognizeChord("c#")).isEqualTo(Chord(1, true, ""))
        assertThat(detector.recognizeChord("d")).isEqualTo(Chord(2, true, ""))

        assertThat(detector.recognizeChord("csus4")).isEqualTo(Chord(0, true, "sus4"))
        assertThat(detector.recognizeChord("c#sus4")).isEqualTo(Chord(1, true, "sus4"))
        assertThat(detector.recognizeChord("dsus4")).isEqualTo(Chord(2, true, "sus4"))

        assertThat(detector.recognizeChord("dis")).isEqualTo(Chord(3, true, ""))
        assertThat(detector.recognizeChord("es")).isEqualTo(Chord(3, true, ""))
        assertThat(detector.recognizeChord("dupa")).isNull()
    }

    @Test
    fun test_detect_fmaj7() {
        val detector = ChordsDetector(ChordsNotation.ENGLISH)
        assertThat(detector.recognizeChord("Fmaj7")).isEqualTo(Chord(5, false, "maj7"))
        assertThat(detector.detectChords("Fmaj7")).isEqualTo("[Fmaj7]")
        assertThat(detector.isWordAChord("Fmaj7")).isTrue()
    }

    @Test
    fun test_c_plus() {
        val detector = ChordsDetector(ChordsNotation.GERMAN)
        assertThat(detector.recognizeChord("C+")).isEqualTo(Chord(0, false, "+"))
        assertThat(detector.isWordAChord("C+")).isTrue()
    }

    @Test
    fun test_c_minus() {
        val detector = ChordsDetector(ChordsNotation.GERMAN)
        assertThat(detector.recognizeChord("C-")).isEqualTo(Chord(0, false, "-"))
        assertThat(detector.isWordAChord("C-")).isTrue()
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

    @Test
    fun test_not_a_chord() {
        var detector = ChordsDetector(ChordsNotation.GERMAN)
        assertThat(detector.isWordAChord("Dupa")).isFalse()
        assertThat(detector.isWordAChord("Cmaj7blahblahblah")).isFalse()
        assertThat(detector.isWordAChord("Dsus888")).isFalse()
        assertThat(detector.isWordAChord(" ")).isFalse()
        assertThat(detector.isWordAChord("")).isFalse()
        detector = ChordsDetector(ChordsNotation.ENGLISH)
        assertThat(detector.recognizeChord("Dmupa")).isNull()
    }

    @Test
    fun test_suffix() {
        val detector = ChordsDetector(ChordsNotation.GERMAN)
        assertThat(detector.isWordAChord("Fmaj7")).isTrue()
        assertThat(detector.isWordAChord("G#maj7-F")).isTrue()
    }

    @Test
    fun test_minor_chords() {
        val detector = ChordsDetector(ChordsNotation.GERMAN)
        assertThat(detector.isWordAChord("cmaj7")).isTrue()
    }

    @Test
    fun test_cis() {
        val detector = ChordsDetector(ChordsNotation.GERMAN_IS)
        assertThat(detector.isWordAChord("cismaj7")).isTrue()
    }

    @Test
    fun test_all_notations() {
        val detector = ChordsDetector()
        assertThat(detector.isWordAChord("cismaj7")).isTrue()
        assertThat(detector.isWordAChord("Cm")).isTrue()
        assertThat(detector.isWordAChord("C#")).isTrue()
        assertThat(detector.isWordAChord("C#add9")).isTrue()
    }

    @Test
    fun test_detect_long_spaced_chords() {
        val detector = ChordsDetector(ChordsNotation.ENGLISH)
        assertThat(detector.detectChords("Am    Fm   G")).isEqualTo("[Am    Fm   G]")
    }

    @Test
    fun test_detect_dashed_chords() {
        val detector = ChordsDetector(ChordsNotation.ENGLISH)
        assertThat(detector.detectChords("Am-G-C")).isEqualTo("[Am-G-C]")
    }

    @Test
    fun test_detect_english_minor() {
        val detector = ChordsDetector(ChordsNotation.ENGLISH)
        assertThat(detector.recognizeChord("Dm")).isEqualTo(Chord(2, true, ""))
        assertThat(detector.recognizeChord("Dmmaj7")).isEqualTo(Chord(2, true, "maj7"))
        assertThat(detector.recognizeChord("Dmaj7")).isEqualTo(Chord(2, false, "maj7"))
        assertThat(detector.recognizeChord("D")).isEqualTo(Chord(2, false, ""))
    }

}
