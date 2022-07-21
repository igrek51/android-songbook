package igrek.songbook.chords.detect

import igrek.songbook.chords.parser.ChordParser
import igrek.songbook.chords.parser.LyricsParser
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions
import org.junit.Test

class UniqueChordsFinderTest {

    @Test
    fun test_whiter_shade_of_pale_unique_chords() {
        val chordsFinder = UniqueChordsFinder()
        val input = listOf(
            "[C C/H a a/G]",
            "[F F/E d d/C]",
            "[G G/F e e/D]",
            "[C F G F G]",
            "[C C/H a a/G]",
            "[F F/E d d/C]",
            "[G G/F e e/D]",
            "[C C/H a a/G]",
            "[F F/E d d/C]",
            "[G G/F e e/D]",
            "[C C/H a a/G]",
            "[F F/E d]",
            "[G7 C C/H a a/G]",
            "[F F/E d d/C]",
            "[G G/F e e/D]",
            "[C F C G6add11 G6]",
        ).joinToString("\n")

        val lyrics = LyricsParser().parseLyrics(input)
        ChordParser(ChordsNotation.GERMAN).parseAndFillChords(lyrics)

        val uniqueChords = chordsFinder.findUniqueChordNamesInLyrics(lyrics)

        Assertions.assertThat(uniqueChords).containsOnly(
            "C", "C/H", "a", "a/G",
            "F", "F/E", "d", "d/C",
            "G", "G/F", "e", "e/D",
            "G6add11", "G6", "G7"
        )
    }

}