package igrek.songbook.chords.detector

import igrek.songbook.chords.syntax.ChordNameProvider
import igrek.songbook.chords.syntax.chordsAllDelimiters
import igrek.songbook.chords.syntax.chordsDelimiters
import igrek.songbook.chords.syntax.longestChordComparator
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.util.lookup.SimpleCache
import java.util.*
import kotlin.math.min

class ChordsDetector(notation: ChordsNotation? = null) {

    private val chordNameProvider = ChordNameProvider()

    var detectedChords = mutableListOf<String>()
        private set

    private val chordsBaseNames: SimpleCache<Set<String>> = SimpleCache {
        val names = sortedSetOf(longestChordComparator)
        if (notation == null) {
            ChordsNotation.values().forEach { notation ->
                names.addAll(chordNameProvider.baseNotesNames(notation).flatten())
                names.addAll(chordNameProvider.lowercaseChords(notation).flatten())
            }
        } else {
            names.addAll(chordNameProvider.baseNotesNames(notation).flatten())
            names.addAll(chordNameProvider.lowercaseChords(notation).flatten())
        }
        names
    }

    private val baseChordToNoteIndex: SimpleCache<Map<String, Int>> = SimpleCache {
        val allNames = hashMapOf<String, Int>()
        if (notation == null) {
            ChordsNotation.values().forEach { notation ->
                chordNameProvider.baseNotesNames(notation).forEachIndexed { index, names ->
                    names.forEach { name ->
                        allNames[name] = index
                    }
                }
            }
        } else {
            chordNameProvider.baseNotesNames(notation).forEachIndexed { index, names ->
                names.forEach { name ->
                    allNames[name] = index
                }
            }
        }
        allNames.toSortedMap(longestChordComparator)
    }

    private val lowercaseChordToNoteIndex: SimpleCache<Map<String, Int>> = SimpleCache {
        val allNames = hashMapOf<String, Int>()
        if (notation == null) {
            ChordsNotation.values().forEach { notation ->
                chordNameProvider.lowercaseChords(notation).forEachIndexed { index, names ->
                    names.forEach { name ->
                        allNames[name] = index
                    }
                }
            }
        } else {
            chordNameProvider.lowercaseChords(notation).forEachIndexed { index, names ->
                names.forEach { name ->
                    allNames[name] = index
                }
            }
        }
        allNames.toSortedMap(longestChordComparator)
    }

    fun detectChords(lyrics: String): String {
        return lyrics.lines().joinToString(separator = "\n") { line ->
            // inverted chords match - find expressions which are not chords
            var line2 = "]$line["
            line2 = line2.replace(Regex("""](.*?)\[""")) { matchResult ->
                "]" + findChordsInSentence(matchResult.groupValues[1]) + "["
            }
            line2.drop(1).dropLast(1)
        }
    }

    fun isWordAChord(word: String): Boolean {
        return isWordAChord(word, chordsDelimiters) or isWordAChord(word, chordsAllDelimiters)
    }

    private fun isWordAChord(word: String, delimiters: Set<String>): Boolean {
        val splitted = word.split(*delimiters.toTypedArray())

        // only delimiters
        if (splitted.all { it.isEmpty() })
            return false

        splitted.forEach { fragment ->
            if (fragment.isNotEmpty()) {
                if (!isWordFragmentAChord(fragment))
                    return false
            }
        }

        return true
    }

    fun recognizeChord(chord: String): Chord? {
        // recognize basic chord (without suffixes)
        var chordNumber: Int? = chordNumbers.get()[chord]
        if (chordNumber != null)
            return Pair(chordNumber, "")

        // recognize exceptions first
        exceptionChords.get().forEach { (exceptionChord, chordBase) ->
            if (chord.startsWith(exceptionChord)) {
                chordNumber = chordNumbers.get()[chordBase]
                val suffix = chord.drop(chordBase.length)
                return Pair(chordNumber!!, suffix)
            }
        }

        // attempt to recognize complex chord (with prefixes): C#maj7, chord + [letters + number]
        // recognize starting from longest ones
        for (l in min(MAX_LENGTH_ANALYZE, chord.length - 1) downTo 1) {
            val chordCut = chord.take(l)
            val suffix = chord.drop(l) // characters appended to a chords, e.g. Cmaj7 -> maj7
            chordNumber = chordNumbers.get()[chordCut]
            if (chordNumber != null)
                return Pair(chordNumber!!, suffix) // a chord with suffix has been recognized
        }

        logger.warn("Chords detector: chord not recognized [${chord.length}]: $chord")
        return null
    }

    private fun isWordFragmentAChord(word: String): Boolean {
        chordBasicNames.get().forEach { chordBase ->
            if (word.startsWith(chordBase)) {
                if (word == chordBase)
                    return true
                // check suffixes
                val remainder = word.drop(chordBase.length)
                for (suffix: String in chordSuffixes) {
                    if (remainder.startsWith(suffix))
                        return true
                }
            }
        }
        return false
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
            detectedChords.add(word)
        }

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


}