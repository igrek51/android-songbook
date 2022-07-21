package igrek.songbook.chordsv2

import igrek.songbook.chordsv2.detect.KeyDetector
import igrek.songbook.chordsv2.detect.UniqueChordsFinder
import igrek.songbook.chordsv2.model.*
import igrek.songbook.chordsv2.parser.ChordParser
import igrek.songbook.chordsv2.parser.LyricsParser
import igrek.songbook.chordsv2.syntax.MajorKey
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class KeyDetectorTest {

    @Test
    fun test_detectLyricsKey() {
        val lyrics = LyricsParser().parseLyrics("""
        dupa [Am F C7/G# G]
        [F] next [Am]verse [G]
        """.trimIndent())

        val chordParser = ChordParser(ChordsNotation.ENGLISH)
        chordParser.parseAndFillChords(lyrics)

        val uniqueNotes = UniqueChordsFinder().findUniqueNotesInLyrics(lyrics)
        assertThat(uniqueNotes).isEqualTo(setOf(
            Note.A.index, Note.F.index, Note.C.index, Note.G.index
        ))

        val key = KeyDetector().detectKey(lyrics)
        assertThat(key).isEqualTo(MajorKey.C_MAJOR)
    }

    @Test
    fun test_detectGMajorKey() {
        val lyrics = LyricsParser().parseLyrics("[F# E D G]")
        ChordParser(ChordsNotation.ENGLISH).parseAndFillChords(lyrics)
        val key = KeyDetector().detectKey(lyrics)
        assertThat(key).isEqualTo(MajorKey.G_MAJOR)
    }

    @Test
    fun test_detectDMajorKey() {
        val lyrics = LyricsParser().parseLyrics("[F# E D A]")
        ChordParser(ChordsNotation.ENGLISH).parseAndFillChords(lyrics)
        val key = KeyDetector().detectKey(lyrics)
        assertThat(key).isEqualTo(MajorKey.D_MAJOR)
    }

    @Test
    fun test_detectMajorKeyWithMinorChord() {
        val lyrics = LyricsParser().parseLyrics("[F#m E D]")
        ChordParser(ChordsNotation.ENGLISH).parseAndFillChords(lyrics)
        val scores = KeyDetector().detectKeyScores(lyrics)
        println(scores)
        val key = KeyDetector().detectKey(lyrics)
        assertThat(key).isEqualTo(MajorKey.D_MAJOR)
    }

    @Test
    fun test_detectAMajorKeyWithMinorChord() {
        val lyrics = LyricsParser().parseLyrics("[F#m E D A]")
        ChordParser(ChordsNotation.ENGLISH).parseAndFillChords(lyrics)
        val scores = KeyDetector().detectKeyScores(lyrics)
        println(scores)
        val key = KeyDetector().detectKey(lyrics)
        assertThat(key).isEqualTo(MajorKey.A_MAJOR)
    }

    @Test
    fun test_detectFMajorKey() {
        val lyrics = LyricsParser().parseLyrics("[Dm C Bb F]")
        ChordParser(ChordsNotation.ENGLISH).parseAndFillChords(lyrics)
        val key = KeyDetector().detectKey(lyrics)
        assertThat(key).isEqualTo(MajorKey.F_MAJOR)
    }

    @Test
    fun test_detectBbMajorKey() {
        val lyrics = LyricsParser().parseLyrics("[Dm C Bb F Eb]")
        ChordParser(ChordsNotation.ENGLISH).parseAndFillChords(lyrics)
        val key = KeyDetector().detectKey(lyrics)
        assertThat(key).isEqualTo(MajorKey.B_FLAT_MAJOR)
    }

    @Test
    fun test_allChromaticChords() {
        val lyrics = LyricsParser().parseLyrics("[C C# D D# E F F# G G# A Bb B]")
        ChordParser(ChordsNotation.ENGLISH).parseAndFillChords(lyrics)
        val scores = KeyDetector().detectKeyScores(lyrics)
        println(scores)
        val key = KeyDetector().detectKey(lyrics)
        assertThat(key).isEqualTo(MajorKey.C_MAJOR)
    }

}