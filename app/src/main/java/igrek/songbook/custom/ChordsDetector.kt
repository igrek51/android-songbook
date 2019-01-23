package igrek.songbook.custom

import igrek.songbook.settings.chordsnotation.ChordsNotation

class ChordsDetector(val notation: ChordsNotation?) {

    companion object {

        private val lengthComparator = Comparator<String> { lhs: String, rhs: String ->
            if (rhs.length != lhs.length) {
                rhs.length - lhs.length
            } else {
                lhs.compareTo(rhs)
            }
        }

        private val chordPrefixes = sortedSetOf(lengthComparator,
                "c", "c#", "d", "d#", "e", "f", "f#", "g", "g#", "a", "b", "h",
                "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "B", "H",

                "cis", "dis", "fis", "gis",
                "Cis", "Dis", "Fis", "Gis",

                "Cm", "C#m", "Dm", "D#m", "Em", "Fm", "F#m", "Gm", "G#m", "Am", "Bbm", "Bm",
                "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "Bb", "B"
        )

        private val chordSuffixes = setOf(
                "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "add", "dim", "sus", "maj", "min",
                "+", "#"
        )

        private val chordDelimiters = setOf(" ", "-", "(", ")", "/", ",", "\n")
    }


    fun checkChords(lyrics: String): String {
        return lyrics.lines().joinToString(separator = "\n") { line ->
            checkChordsLine(line)
        }
    }

    private fun checkChordsLine(line: String): String {
        return line.split(" ").joinToString(separator = " ") { word ->
            if (isChordsWord(word))
                "[$word]"
            else
                word
        }
    }

    private fun isChordsWord(word: String): Boolean {
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