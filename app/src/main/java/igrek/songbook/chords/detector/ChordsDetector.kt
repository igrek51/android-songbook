package igrek.songbook.chords.detector

import igrek.songbook.chords.syntax.*
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.util.lookup.SimpleCache

class ChordsDetector(notation: ChordsNotation? = null) {

    private val chordNameProvider = ChordNameProvider()

    var detectedChords = mutableListOf<String>()
        private set

    private val baseChordNames: SimpleCache<Set<String>> = SimpleCache {
        val names = sortedSetOf(longestChordComparator)
        if (notation == null) {
            ChordsNotation.values().forEach { notation ->
                names.addAll(chordNameProvider.baseNotesNames(notation).flatten())
            }
        } else {
            names.addAll(chordNameProvider.baseNotesNames(notation).flatten())
        }
        names
    }

    private val minorChordNames: SimpleCache<Set<String>> = SimpleCache {
        val names = sortedSetOf(longestChordComparator)
        if (notation == null) {
            ChordsNotation.values().forEach { notation ->
                names.addAll(chordNameProvider.minorChords(notation).flatten())
            }
        } else {
            names.addAll(chordNameProvider.minorChords(notation).flatten())
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

    private val minorChordToNoteIndex: SimpleCache<Map<String, Int>> = SimpleCache {
        val allNames = hashMapOf<String, Int>()
        if (notation == null) {
            ChordsNotation.values().forEach { notation ->
                chordNameProvider.minorChords(notation).forEachIndexed { index, names ->
                    names.forEach { name ->
                        allNames[name] = index
                    }
                }
            }
        } else {
            chordNameProvider.minorChords(notation).forEachIndexed { index, names ->
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
        return isWordAChord(word, chordsPrimaryDelimiters) or isWordAChord(word, chordsAllDelimiters)
    }

    private fun isWordAChord(word: String, delimiters: Array<String>): Boolean {
        val splitted = word.split(*delimiters)

        // only delimiters word
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

    private fun isWordFragmentAChord(word: String): Boolean {
        minorChordNames.get().forEach { chordBase ->
            if (word == chordBase)
                return true
            if (word.startsWith(chordBase)) {
                val remainder = word.drop(chordBase.length)
                if (remainder in chordSuffixes)
                    return true
            }
        }
        baseChordNames.get().forEach { chordBase ->
            if (word == chordBase)
                return true
            if (word.startsWith(chordBase)) {
                val remainder = word.drop(chordBase.length)
                if (remainder in chordSuffixes)
                    return true
            }
        }
        return false
    }

    fun recognizeChord(chord: String): Chord? {
        // recognize basic chord (without suffixes)
        minorChordToNoteIndex.get()[chord]?.let {
            return Chord(it, minor = true)
        }
        baseChordToNoteIndex.get()[chord]?.let {
            return Chord(it, minor = false)
        }

        // recognize base chord + suffix
        minorChordToNoteIndex.get().forEach { (baseName: String, noteIndex: Int) ->
            if (chord.startsWith(baseName)) {
                val suffix = chord.drop(baseName.length)
                if (suffix in chordSuffixes)
                    return Chord(noteIndex, minor = true, suffix = suffix)
            }
        }
        baseChordToNoteIndex.get().forEach { (baseName: String, noteIndex: Int) ->
            if (chord.startsWith(baseName)) {
                val suffix = chord.drop(baseName.length)
                if (suffix in chordSuffixes)
                    return Chord(noteIndex, minor = false, suffix = suffix)
            }
        }

        logger.warn("Chords detector: chord not recognized: \"$chord\"")
        return null
    }

    private fun findChordsInSentence(sentence: String): String {
        // seek chords from right to left
        val words = sentence.split(" ").filter { it.isNotEmpty() }
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
        val wordsNotChords = words.dropLast(chordsFound)

        return wordsNotChords.joinToString(separator = " ") +
                " [" + chords.joinToString(separator = " ") + "]"
    }

}