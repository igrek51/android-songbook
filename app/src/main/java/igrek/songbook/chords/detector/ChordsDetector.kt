package igrek.songbook.chords.detector

import igrek.songbook.chords.ChordNameProvider
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.system.cache.SimpleCache
import java.util.*

class ChordsDetector(notation: ChordsNotation? = null) {

    companion object {
        /**
         * supported chords formats:
         * d, d#, D, D#, Dm, D#m, Dmaj7, D#maj7, d7, d#7, D#m7, D#7, Dadd9, Dsus
         */
        private val chordSuffixes = setOf(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                "add", "dim", "sus", "maj", "min", "aug",
                "Major", "6add9", "m7", "m9", "m11", "m13", "m6", "madd9", "mmaj7", "mmaj9",
                "+", "#", "-", "(", ")", "/", "\n")

        const val MAX_LENGTH_ANALYZE = 3

        private val longestFirstComparator = Comparator<String> { lhs: String, rhs: String ->
            if (rhs.length != lhs.length) {
                rhs.length - lhs.length
            } else {
                lhs.compareTo(rhs)
            }
        }
    }

    private val chordNameProvider = ChordNameProvider()
    var detectedChords = 0

    private val chordPrefixes: SimpleCache<Set<String>> = SimpleCache {
        val prefixes = sortedSetOf(longestFirstComparator)
        prefixes.addAll(chordNameProvider.allChords(notation))
        prefixes
    }

    /**
     * map: chord name (prefix) -> chord number
     * keys sorted by length descending
     */
    private val chordNumbers: SimpleCache<Map<String, Int>> = SimpleCache {
        val nameToIndex = TreeMap<String, Int>(longestFirstComparator)
        val maxChordNumber = 12 * 2 // minor and major chords
        chordNameProvider.allChords(notation).forEachIndexed { index, name ->
            nameToIndex[name] = index % maxChordNumber
        }
        nameToIndex
    }

    fun findChords(lyrics: String): String {
        return lyrics.lines().joinToString(separator = "\n") { line ->
            // inverted chords match - find expressions which are not chords
            var line2 = "]$line["
            line2 = line2.replace(Regex("""](.*?)\[""")) { matchResult ->
                "]" + findChordsInSentence(matchResult.groupValues[1]) + "["
            }
            line2.drop(1).dropLast(1)
        }
    }

    private fun findChordsInSentence(sentence: String): String {
        // seek chords from right to left
        val words = sentence.split(" ")
        var chordsFound = 0
        for (i in words.size - 1 downTo 0) {
            val word = words[i]
            if (!isWordAChord(word))
                break
            chordsFound++
        }
        detectedChords += chordsFound

        if (chordsFound == 0) { // no chords
            return sentence
        }
        if (chordsFound == words.size) { // all chords
            return "[$sentence]"
        }

        val chords = words.takeLast(chordsFound)
        val words2 = words.dropLast(chordsFound)

        return words2.joinToString(separator = " ") +
                " [" + chords.joinToString(separator = " ") + "]"
    }

    fun isWordAChord(word: String): Boolean {
        for (prefix: String in chordPrefixes.get()) {
            if (word.startsWith(prefix)) {
                if (word == prefix)
                    return true
                // check suffixes
                val remainder = word.drop(prefix.length)
                for (suffix: String in chordSuffixes) {
                    if (remainder.startsWith(suffix))
                        return true
                }
            }
        }
        return false
    }

    /**
     * @param chord chord in format: C, C#, c, c#, Cmaj7, c7, c#7, Cadd9, Csus
     * @return pair of recognized chord number with suffix or null if not recognized any
     */
    fun recognizeChord(chord: String): Pair<Int, String>? {
        var chordNumber: Int? = chordNumbers.get()[chord]
        if (chordNumber != null)
            return Pair(chordNumber, "")
        // basic chord (without suffixes) was not recognized
        // attempt to recognize complex chord (with prefixes): C#maj7, chord + [letters + number]
        // recognize starting from longest ones
        for (l in Math.min(MAX_LENGTH_ANALYZE, chord.length - 1) downTo 1) {
            val chordCut = chord.take(l)
            val suffix = chord.drop(l) // characters appended to a chords, e.g. Cmaj7 -> maj7
            chordNumber = chordNumbers.get()[chordCut]
            if (chordNumber != null)
                return Pair(chordNumber, suffix) // a chord with suffix has been recognized
        }

        logger.warn("Chords detector: chord not recognized [${chord.length}]: $chord")
        return null
    }

}