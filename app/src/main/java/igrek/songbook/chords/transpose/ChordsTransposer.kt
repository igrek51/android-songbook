package igrek.songbook.chords.transpose

import igrek.songbook.chords.ChordNameProvider
import igrek.songbook.chords.ChordsDetector
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.system.cache.SimpleCache
import java.util.*

class ChordsTransposer(private val chordsNotation: ChordsNotation) {

    companion object {

        const val MAX_LENGTH_ANALYZE = 2

        private val lengthComparator = { lhs: String, rhs: String ->
            if (rhs.length != lhs.length) {
                rhs.length - lhs.length
            } else {
                lhs.compareTo(rhs)
            }
        }

    }

    private val logger = LoggerFactory.logger

    private val chordNameProvider = ChordNameProvider()

    private val chordsNotationSoundNames: SimpleCache<List<String>> = SimpleCache {
        chordNameProvider.allChords(chordsNotation)
    }

    /**
     * map: chord name (prefix) -> chord number
     * keys sorted by length descending
     */
    private val germanNumbersMap: SimpleCache<Map<String, Int>> = SimpleCache {
        val nameToIndex = TreeMap<String, Int>(lengthComparator)
        chordNameProvider.allChords(ChordsNotation.GERMAN).forEachIndexed { index, name ->
            nameToIndex[name] = index
        }
        nameToIndex
    }

    /**
     * @param input file content with lyrics and chords
     * @param t  shift semitones count
     * @return content transposed by semitones (the same format as input)
     */
    fun transposeContent(input: String, t: Int): String {
        val out = StringBuilder()
        val chordsSection = StringBuilder()
        var bracket = false

        for (i in 0 until input.length) {
            val c = input[i]
            if (c == '[') {
                bracket = true
                out.append(c)
            } else if (c == ']') {
                bracket = false
                // transpose whole chords section
                val transposedChords = transposeChords(chordsSection.toString(), t)
                out.append(transposedChords)
                out.append(c)
                chordsSection.delete(0, chordsSection.length)
            } else { // the regular character
                if (bracket) {
                    chordsSection.append(c) // move to chords section buffer
                } else {
                    out.append(c)
                }
            }
        }

        return out.toString()
    }

    private fun getChordNumber(chord: String): Int? {
        return germanNumbersMap.get()[chord]
    }

    private fun getChordName(chordNr: Int): String? {
        val soundNames = chordsNotationSoundNames.get()
        return if (chordNr < 0 || chordNr >= soundNames.size) null else soundNames[chordNr]
    }

    /**
     * @param input chords section
     * @param t  shift semitones count
     * @return chords section transposed by semitones
     */
    private fun transposeChords(input: String, t: Int): String {
        val out = StringBuilder()
        val chords = splitWithDelimiters(input, ChordsDetector.chordsDelimiters)
        for (chord in chords) {
            val transposedChord = transposeChord(chord.str, t)
            out.append(transposedChord)
            out.append(chord.delimiter) // append the same delimiter which was splitting a chord
        }

        return out.toString()
    }

    /**
     * @param chord chord in format: C, C#, c, c#, Cmaj7, c7, c#7, Cadd9, Csus
     * @param t     shift semitones count
     * @return chord transposed by semitones
     */
    private fun transposeChord(chord: String, t: Int): String {
        // TODO repeated code: move to chords detector
        if (chord.trim { it <= ' ' }.isEmpty())
            return chord

        // chord recognition
        var chordNumber = getChordNumber(chord)
        var suffix = "" // characters appended to a chords, e.g. Cmaj7 (maj7)
        if (chordNumber == null) { // basic chord not recognized (without suffixes)
            // attempt to recognize complex chord (with prefixes): C#maj7, akord + [letters] + [number]
            // recognition shorter and shorter substrings
            for (l in Math.min(MAX_LENGTH_ANALYZE, chord.length - 1) downTo 1) {
                val chordCut = chord.substring(0, l)
                suffix = chord.substring(l)
                chordNumber = getChordNumber(chordCut)
                if (chordNumber != null)
                    break // a chord with suffix has been recognized
            }
            if (chordNumber == null) { // a chord was not recognized
                logger.warn("Transpose: Chord not recognized [" + chord.length + "]: " + chord)
                return chord
            }
        }

        // transpose by semitones
        val family = getChordFamilyIndex(chordNumber)
        chordNumber += t
        // restore the original chord family
        while (getChordFamilyIndex(chordNumber) > family)
            chordNumber -= 12
        while (chordNumber < 0 || getChordFamilyIndex(chordNumber) < family)
            chordNumber += 12

        return getChordName(chordNumber)!! + suffix
    }

    private fun getChordFamilyIndex(chordNumber: Int): Int {
        return chordNumber / 12
    }

    /**
     * @param input         text with separators
     * @param delimiters delimiters (separators) table
     * @return a list of splitted text fragments with delimiters stored (or without if it's the last part)
     */
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
