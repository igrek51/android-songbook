package igrek.songbook.chords

import igrek.songbook.chords.detector.ChordsDetector
import igrek.songbook.chords.splitter.ChordsSplitter
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.system.cache.SimpleCache

class ChordsConverter(fromNotation: ChordsNotation, toNotation: ChordsNotation) {

    private val chordNameProvider = ChordNameProvider()
    private val chordsSplitter = ChordsSplitter()
    private val fromChordsDetector = ChordsDetector(fromNotation)

    private val toChordsNames: SimpleCache<List<String>> = SimpleCache {
        chordNameProvider.allChords(toNotation)
    }

    fun convertLyrics(lyrics: String): String {
        return lyrics.lines().joinToString(separator = "\n") { line ->
            line.replace(Regex("""\[(.*?)]""")) { matchResult ->
                "[" + convertChords(matchResult.groupValues[1]) + "]"
            }
        }
    }

    fun convertChords(chordsSection: String): String {
        val out = StringBuilder()
        val delimitedChords = chordsSplitter.splitWithDelimiters(chordsSection)
        for (delimitedChord in delimitedChords) {
            out.append(convertChord(delimitedChord.str))
            out.append(delimitedChord.delimiter) // append the same delimiter which was splitting a chord
        }
        return out.toString()
    }

    fun convertChord(chord: String): String {
        if (chord.trim { it <= ' ' }.isEmpty())
            return chord

        val recognized = fromChordsDetector.recognizeChord(chord) ?: return chord

        val chordNumber = recognized.first
        val suffix = recognized.second
        val convertedChord = toChordsNames.get()[chordNumber]
        return convertedChord + suffix
    }

}