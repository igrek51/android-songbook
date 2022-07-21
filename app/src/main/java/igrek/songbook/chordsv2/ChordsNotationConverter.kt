package igrek.songbook.chordsv2

import igrek.songbook.chordsv2.detect.KeyDetector
import igrek.songbook.chordsv2.model.LyricsFragment
import igrek.songbook.chordsv2.model.LyricsTextType
import igrek.songbook.chordsv2.parser.ChordParser
import igrek.songbook.chordsv2.parser.LyricsParser
import igrek.songbook.settings.chordsnotation.ChordsNotation

class ChordsNotationConverter(
    private val fromNotation: ChordsNotation,
    private val toNotation: ChordsNotation,
) {

    fun convertLyrics(input: String, originalModifiers: Boolean = false): String {
        val lyrics = LyricsParser().parseLyrics(input)
        ChordParser(fromNotation).parseAndFillChords(lyrics)
        val key = KeyDetector().detectKey(lyrics)
        ChordsFormatter(toNotation, key).formatLyrics(lyrics, originalModifiers)
        return lyrics.displayString()
    }

    fun convertSingleChord(chord: String, originalModifiers: Boolean = false): String {
        val unknowns = mutableSetOf<String>()
        val chordFragments = ChordParser(fromNotation).parseSingleChordFragments(chord, unknowns)
        val lyricsFragment = LyricsFragment(chord, LyricsTextType.CHORDS, chordFragments=chordFragments)
        ChordsFormatter(toNotation).renderLyricsFragment(lyricsFragment, originalModifiers)
        return lyricsFragment.text
    }

}