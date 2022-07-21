package igrek.songbook.chords.converter

import igrek.songbook.chords.detect.KeyDetector
import igrek.songbook.chords.render.ChordsRenderer
import igrek.songbook.chords.model.LyricsFragment
import igrek.songbook.chords.model.LyricsTextType
import igrek.songbook.chords.parser.ChordParser
import igrek.songbook.chords.parser.LyricsExtractor
import igrek.songbook.settings.chordsnotation.ChordsNotation

class ChordsNotationConverter(
    private val fromNotation: ChordsNotation,
    private val toNotation: ChordsNotation,
) {

    fun convertLyrics(input: String, originalModifiers: Boolean = false): String {
        val lyrics = LyricsExtractor().parseLyrics(input)
        ChordParser(fromNotation).parseAndFillChords(lyrics)
        val key = KeyDetector().detectKey(lyrics)
        ChordsRenderer(toNotation, key).formatLyrics(lyrics, originalModifiers)
        return lyrics.displayString()
    }

    fun convertChordFragments(chord: String, originalModifiers: Boolean = false): String {
        val unknowns = mutableSetOf<String>()
        val chordFragments = ChordParser(fromNotation).parseSingleChordFragments(chord, unknowns)
        val lyricsFragment = LyricsFragment(chord, LyricsTextType.CHORDS, chordFragments=chordFragments)
        ChordsRenderer(toNotation).renderLyricsFragment(lyricsFragment, originalModifiers)
        return lyricsFragment.text
    }

}