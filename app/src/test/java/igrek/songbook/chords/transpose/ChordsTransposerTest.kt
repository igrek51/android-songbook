package igrek.songbook.chords.transpose

import igrek.songbook.chords.render.ChordsRenderer
import igrek.songbook.chords.detect.KeyDetector
import igrek.songbook.chords.parser.ChordParser
import igrek.songbook.chords.parser.LyricsParser
import igrek.songbook.chords.syntax.MajorKey
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ChordsTransposerTest {

    @Test
    fun test_noTransposition() {
        val input = "a b c d [e f G Ab B C]"
        val lyrics = LyricsParser().parseLyrics(input)
        ChordParser(ChordsNotation.GERMAN).parseAndFillChords(lyrics)

        var transposedLyrics = ChordsTransposer().transposeLyrics(lyrics, 0)
        var key = KeyDetector().detectKey(transposedLyrics)
        ChordsRenderer(ChordsNotation.GERMAN, key).formatLyrics(transposedLyrics)
        assertThat(transposedLyrics.displayString()).isEqualTo(input)

        transposedLyrics = ChordsTransposer().transposeLyrics(lyrics, 12)
        key = KeyDetector().detectKey(transposedLyrics)
        ChordsRenderer(ChordsNotation.GERMAN, key).formatLyrics(transposedLyrics)
        assertThat(transposedLyrics.displayString()).isEqualTo(input)

        transposedLyrics = ChordsTransposer().transposeLyrics(lyrics, -12)
        key = KeyDetector().detectKey(transposedLyrics)
        ChordsRenderer(ChordsNotation.GERMAN, key).formatLyrics(transposedLyrics)
        assertThat(transposedLyrics.displayString()).isEqualTo(input)
    }

    @Test
    fun test_transposePlus1() {
        val input = "a b [e f G Ab B C]"
        val lyrics = LyricsParser().parseLyrics(input)
        ChordParser(ChordsNotation.GERMAN).parseAndFillChords(lyrics)

        val transposedLyrics = ChordsTransposer().transposeLyrics(lyrics, 1)
        val key = KeyDetector().detectKey(transposedLyrics)
        println("Key is $key")
        ChordsRenderer(ChordsNotation.GERMAN, key).formatLyrics(transposedLyrics)
        assertThat(transposedLyrics.displayString()).isEqualTo("a b [f f# G# A H C#]")
    }

    @Test
    fun test_transposeMinus1() {
        val input = "a b [f f# G# A H C#]"
        val lyrics = LyricsParser().parseLyrics(input)
        ChordParser(ChordsNotation.GERMAN).parseAndFillChords(lyrics)

        val transposedLyrics = ChordsTransposer().transposeLyrics(lyrics, -1)
        val key = KeyDetector().detectKey(transposedLyrics)
        println("Key is $key")
        ChordsRenderer(ChordsNotation.GERMAN, key).formatLyrics(transposedLyrics)
        assertThat(transposedLyrics.displayString()).isEqualTo("a b [e f G Ab B C]")
    }

    @Test
    fun test_englishChordsTranpose() {
        val input = "a b [e f G Ab B C]"
        val lyrics = LyricsParser().parseLyrics(input)
        ChordParser(ChordsNotation.GERMAN).parseAndFillChords(lyrics)

        var transposedLyrics = ChordsTransposer().transposeLyrics(lyrics, 0)
        var key = KeyDetector().detectKey(transposedLyrics)
        assertThat(key).isEqualTo(MajorKey.A_FLAT_MAJOR)
        ChordsRenderer(ChordsNotation.ENGLISH, key).formatLyrics(transposedLyrics)
        assertThat(transposedLyrics.displayString()).isEqualTo("a b [Em Fm G Ab Bb C]")

        transposedLyrics = ChordsTransposer().transposeLyrics(lyrics, 1)
        key = KeyDetector().detectKey(transposedLyrics)
        assertThat(key).isEqualTo(MajorKey.A_MAJOR)
        ChordsRenderer(ChordsNotation.ENGLISH, key).formatLyrics(transposedLyrics)
        assertThat(transposedLyrics.displayString()).isEqualTo("a b [Fm F#m G# A B C#]")
    }

    @Test
    fun test_germanFisChordsTranpose() {
        val input = "a b [e f G7 G# B H]"
        val lyrics = LyricsParser().parseLyrics(input)
        ChordParser(ChordsNotation.GERMAN_IS).parseAndFillChords(lyrics)

        var transposedLyrics = ChordsTransposer().transposeLyrics(lyrics, 0)
        var key = KeyDetector().detectKey(transposedLyrics)
        assertThat(key).isEqualTo(MajorKey.A_FLAT_MAJOR)
        ChordsRenderer(ChordsNotation.GERMAN_IS, key).formatLyrics(transposedLyrics)
        assertThat(transposedLyrics.displayString()).isEqualTo("a b [e f G7 As B H]")

        transposedLyrics = ChordsTransposer().transposeLyrics(lyrics, 1)
        key = KeyDetector().detectKey(transposedLyrics)
        assertThat(key).isEqualTo(MajorKey.A_MAJOR)
        ChordsRenderer(ChordsNotation.GERMAN_IS, key).formatLyrics(transposedLyrics)
        assertThat(transposedLyrics.displayString()).isEqualTo("a b [f fis Gis7 A H C]")
    }

    private fun quickTranspose(input: String, notation: ChordsNotation, t: Int): String {
        val lyrics = LyricsParser().parseLyrics(input)
        ChordParser(notation).parseAndFillChords(lyrics)
        val transposedLyrics = ChordsTransposer().transposeLyrics(lyrics, t)
        val originalKey = KeyDetector().detectKey(lyrics)
        val newKey = KeyDetector().detectKey(transposedLyrics)
        println("Transposing from $originalKey to $newKey")
        ChordsRenderer(notation, newKey).formatLyrics(transposedLyrics)
        return transposedLyrics.displayString()
    }

    @Test
    fun test_parentheses_Ch() {
        val transposed = quickTranspose("[(C-h)]", ChordsNotation.GERMAN, 2)
        assertThat(transposed).isEqualTo("[(D-c#)]")
    }

    @Test
    fun test_transposeGermanChords() {
        assertThat(quickTranspose("[a]", ChordsNotation.GERMAN, 0)).isEqualTo("[a]")
        assertThat(quickTranspose("[a]", ChordsNotation.GERMAN, 1)).isEqualTo("[b]")
        assertThat(quickTranspose("[A]", ChordsNotation.GERMAN, 1)).isEqualTo("[B]")
        assertThat(quickTranspose("[A]", ChordsNotation.GERMAN, 2)).isEqualTo("[H]")
        assertThat(quickTranspose("[A]", ChordsNotation.GERMAN, 3)).isEqualTo("[C]")
        assertThat(quickTranspose("[A]", ChordsNotation.GERMAN, 4)).isEqualTo("[Db]")
        assertThat(quickTranspose("[not_a_chord]", ChordsNotation.GERMAN, 4)).isEqualTo("[not_a_chord]")
    }

    @Test
    fun test_transposeLyrics_German() {
        assertThat(quickTranspose("[a]", ChordsNotation.GERMAN, 0)).isEqualTo("[a]")
        assertThat(quickTranspose("[a]", ChordsNotation.GERMAN, 1)).isEqualTo("[b]")
        assertThat(quickTranspose("[a]", ChordsNotation.GERMAN, 3)).isEqualTo("[c]")
        assertThat(quickTranspose("[A]", ChordsNotation.GERMAN, 3)).isEqualTo("[C]")
    }

    @Test
    fun test_splitting_chords() {
        assertThat(quickTranspose("[a-F-C\na-C/G]", ChordsNotation.GERMAN, 2)).isEqualTo("[h-G-D]\n[h-D/A]")
    }

    @Test
    fun test_transposing_with_newlines() {
        assertThat(quickTranspose("[a\nF\n]", ChordsNotation.GERMAN, 2)).isEqualTo("[h]\n[G]")
    }

    @Test
    fun test_convert_german_moll_to_english() {
        val lyrics = LyricsParser().parseLyrics("[e]")
        ChordParser(ChordsNotation.GERMAN).parseAndFillChords(lyrics)
        val transposedLyrics = ChordsTransposer().transposeLyrics(lyrics, 0)
        val newKey = KeyDetector().detectKey(transposedLyrics)
        ChordsRenderer(ChordsNotation.ENGLISH, newKey).formatLyrics(transposedLyrics)
        assertThat(transposedLyrics.displayString()).isEqualTo("[Em]")
    }
}
