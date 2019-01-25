package igrek.songbook.custom

import igrek.songbook.settings.chordsnotation.ChordsNotation

class ChordsDetector(notation: ChordsNotation?) {

    companion object {

        private val lengthComparator = Comparator<String> { lhs: String, rhs: String ->
            if (rhs.length != lhs.length) {
                rhs.length - lhs.length
            } else {
                lhs.compareTo(rhs)
            }
        }

        private val chordSuffixes = setOf(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
                "add", "dim", "sus", "maj", "min", "aug",
                "Major", "6add9", "m7", "m9", "m11", "m13", "m6", "madd9", "mmaj7", "mmaj9",
                "+", "#", "-", "(", ")", "/", "\n")
    }

    private var chordPrefixes = sortedSetOf(lengthComparator)

    init {
        if (notation == null) {
            ChordsNotation.values().forEach { notation ->
                addChordPrefixes(notation)
            }
        } else {
            addChordPrefixes(notation)
        }
    }

    private fun addChordPrefixes(notation: ChordsNotation) {
        val prefixes = when (notation) {
            ChordsNotation.GERMAN -> setOf(
                    "c", "c#", "d", "d#", "e", "f", "f#", "g", "g#", "a", "b", "h",
                    "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "B", "H"
            )
            ChordsNotation.GERMAN_IS -> setOf(
                    "c", "cis", "d", "dis", "e", "f", "fis", "g", "gis", "a", "b", "h",
                    "C", "Cis", "D", "Dis", "E", "F", "Fis", "G", "Gis", "A", "B", "H"
            )
            ChordsNotation.ENGLISH -> setOf(
                    "Cm", "C#m", "Dm", "D#m", "Em", "Fm", "F#m", "Gm", "G#m", "Am", "Bbm", "Bm",
                    "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "Bb", "B"
            )
        }
        chordPrefixes.addAll(prefixes)
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