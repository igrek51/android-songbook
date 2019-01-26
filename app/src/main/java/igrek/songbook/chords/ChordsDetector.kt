package igrek.songbook.chords

import igrek.songbook.settings.chordsnotation.ChordsNotation

class ChordsDetector(notation: ChordsNotation?) {

    companion object {
        val chordsDelimiters = arrayOf(" ", "-", "(", ")", "/", ",", "\n")

        /**
         * supported chords formats:
         * d, d#, D, D#, Dm, D#m, Dmaj7, D#maj7, d7, d#7, D#m7, D#7, Dadd9, Dsus
         */
        private val chordSuffixes = setOf(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                "add", "dim", "sus", "maj", "min", "aug",
                "Major", "6add9", "m7", "m9", "m11", "m13", "m6", "madd9", "mmaj7", "mmaj9",
                "+", "#", "-", "(", ")", "/", "\n")

        private val lengthComparator = Comparator<String> { lhs: String, rhs: String ->
            if (rhs.length != lhs.length) {
                rhs.length - lhs.length
            } else {
                lhs.compareTo(rhs)
            }
        }
    }

    private var chordPrefixes = sortedSetOf(lengthComparator)

    init {
        val nameProvider = ChordNameProvider()
        if (notation == null) {
            chordPrefixes.addAll(nameProvider.allChords())
        } else {
            chordPrefixes.addAll(nameProvider.allChords(notation))
        }
    }

    fun checkChords(lyrics: String): String {
        return lyrics.lines().joinToString(separator = "\n") { line ->
            // inverted chords match - find expressions which are not chords
            var line2 = "]$line["
            line2 = line2.replace(Regex("""](.*?)\[""")) { matchResult ->
                "]" + checkChordsSentence(matchResult.groupValues[1]) + "["
            }
            line2.drop(1).dropLast(1)
        }
    }

    private fun checkChordsSentence(sentence: String): String {
        // seek chords from right to left
        val words = sentence.split(" ")
        var chordsFound = 0
        for (i in words.size - 1 downTo 0) {
            val word = words[i]
            if (!isWordAChord(word))
                break
            chordsFound++
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

    fun isWordAChord(word: String): Boolean {
        for (prefix: String in chordPrefixes) {
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

}