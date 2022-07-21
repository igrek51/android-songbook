package igrek.songbook.chords.detect

import igrek.songbook.chords.parser.ChordParser
import igrek.songbook.editor.ChordsMarker
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions
import org.junit.Test

class ChordsMarkerTest {

    @Test
    fun test_detect_fmaj7() {
        val detector = ChordParser(ChordsNotation.ENGLISH)
        val chordsMarker = ChordsMarker(detector)
        Assertions.assertThat(chordsMarker.detectAndMarkChords("Fmaj7")).isEqualTo("[Fmaj7]")
        Assertions.assertThat(detector.isWordAChord("Fmaj7")).isTrue
    }

    @Test
    fun test_mark_chords() {
        val detector = ChordParser(ChordsNotation.ENGLISH)
        val chordsMarker = ChordsMarker(detector)
        Assertions.assertThat(chordsMarker.detectAndMarkChords("Fm")).isEqualTo("[Fm]")
    }

    @Test
    fun test_detect_long_spaced_chords() {
        val detector = ChordParser(ChordsNotation.ENGLISH)
        val chordsMarker = ChordsMarker(detector)
        Assertions.assertThat(chordsMarker.detectAndMarkChords("Am    Fm   G")).isEqualTo("[Am]    [Fm]   [G]")
    }

    @Test
    fun test_detect_dashed_chords() {
        val detector = ChordParser(ChordsNotation.ENGLISH)
        val chordsMarker = ChordsMarker(detector)
        Assertions.assertThat(chordsMarker.detectAndMarkChords("Am-G-C")).isEqualTo("[Am-G-C]")
    }

    @Test
    fun test_detect_trimmed_chords() {
        val detector = ChordParser(ChordsNotation.GERMAN)
        val chordsMarker = ChordsMarker(detector)
        Assertions.assertThat(chordsMarker.detectAndMarkChords("  a  ")).isEqualTo("  [a]  ")
    }

    @Test
    fun test_mark_chords_separated_spaces() {
        val detector = ChordParser(ChordsNotation.GERMAN)
        val chordsMarker = ChordsMarker(detector)
        Assertions.assertThat(chordsMarker.detectAndMarkChords(" a   F C  ")).isEqualTo(" [a]   [F] [C]  ")
    }

    @Test
    fun test_stop_marking_on_word() {
        val detector = ChordParser(ChordsNotation.GERMAN)
        val chordsMarker = ChordsMarker(detector)
        Assertions.assertThat(chordsMarker.detectAndMarkChords("jasna dupa a Cmaj7")).isEqualTo("jasna dupa [a] [Cmaj7]")
    }

    @Test
    fun skipMarkingWhenAlreadyChords() {
        val detector = ChordParser(ChordsNotation.GERMAN)
        val chordsMarker = ChordsMarker(detector)
        Assertions.assertThat(chordsMarker.detectAndMarkChords("A word word a [Cmaj7]")).isEqualTo("A word word a [Cmaj7]")
    }

}