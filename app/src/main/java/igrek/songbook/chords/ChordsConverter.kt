package igrek.songbook.chords

import igrek.songbook.chords.transpose.StringWithDelimiter
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.system.cache.SimpleCache
import java.util.*

class ChordsConverter(fromNotation: ChordsNotation, toNotation: ChordsNotation) {

    companion object {
        const val MAX_LENGTH_ANALYZE = 2

        private val longestFirstComparator = Comparator<String> { lhs: String, rhs: String ->
            if (rhs.length != lhs.length) {
                rhs.length - lhs.length
            } else {
                lhs.compareTo(rhs)
            }
        }
    }

    private val chordNameProvider = ChordNameProvider()

    private val fromChordsNumbers: SimpleCache<Map<String, Int>> = SimpleCache {
        val nameToIndex = TreeMap<String, Int>(longestFirstComparator)
        chordNameProvider.allChords(fromNotation).forEachIndexed { index, name ->
            nameToIndex[name] = index
        }
        nameToIndex
    }

    private val toChordsNames: SimpleCache<List<String>> = SimpleCache {
        chordNameProvider.allChords(toNotation)
    }

    fun convertLyrics(lyrics: String): String {
        return lyrics.lines().joinToString(separator = "\n") { line ->
            line.replace(Regex("""\[(.*?)\]""")) { matchResult ->
                "[" + convertChords(matchResult.groupValues[1]) + "]"
            }
        }
    }

    fun convertChords(chordsSection: String): String {
        val out = StringBuilder()
        val delimitedChords = splitWithDelimiters(chordsSection, ChordsDetector.chordsDelimiters)
        for (delimitedChord in delimitedChords) {
            out.append(convertChord(delimitedChord.str))
            out.append(delimitedChord.delimiter) // append the same delimiter which was splitting a chord
        }
        return out.toString()
    }

    fun convertChord(chord: String): String {
        if (chord.trim { it <= ' ' }.isEmpty())
            return chord

        // chord recognition
        var chordNumber: Int? = fromChordsNumbers.get()[chord]
        var suffix = "" // characters appended to a chords, e.g. Cmaj7 (maj7)
        if (chordNumber == null) { // basic chord not recognized (without suffixes)
            // attempt to recognize complex chord (with prefixes): C#maj7, akord + [letters] + [number]
            // recognition shorter and shorter substrings
            for (l in Math.min(MAX_LENGTH_ANALYZE, chord.length - 1) downTo 1) {
                val chordCut = chord.substring(0, l)
                suffix = chord.substring(l)
                chordNumber = fromChordsNumbers.get()[chordCut]
                if (chordNumber != null)
                    break // a chord with suffix has been recognized
            }
            if (chordNumber == null) { // a chord was not recognized
                logger.warn("Chords detector: chord not recognized [" + chord.length + "]: " + chord)
                return chord
            }
        }

        return toChordsNames.get()[chordNumber] + suffix
    }

    private fun splitWithDelimiters(input: String, delimiters: Array<String>): List<StringWithDelimiter> {
        val splitted = ArrayList<StringWithDelimiter>()

        // find a first delimiter
        var minDelimiter: String? = null
        var minDelimiterIndex: Int? = null
        for (delimiter in delimiters) {
            val firstIndex = input.indexOf(delimiter)
            if (firstIndex != -1) {
                if (minDelimiterIndex == null || firstIndex < minDelimiterIndex) {
                    minDelimiterIndex = firstIndex
                    minDelimiter = delimiter
                }
            }
        }

        if (minDelimiterIndex == null) { //no delimiter
            splitted.add(StringWithDelimiter(input)) // the last fragment
            return splitted
        } else {
            val before = input.substring(0, minDelimiterIndex)
            val after = input.substring(minDelimiterIndex + minDelimiter!!.length)
            splitted.add(StringWithDelimiter(before, minDelimiter))
            // recursive split
            splitted.addAll(splitWithDelimiters(after, delimiters))
        }

        return splitted
    }


}