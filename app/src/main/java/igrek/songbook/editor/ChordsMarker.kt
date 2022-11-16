package igrek.songbook.editor

import igrek.songbook.chords.parser.ChordParser

class ChordsMarker(
    private val chordParser: ChordParser,
) {

    var allMarkedChords = mutableListOf<String>()
        private set

    fun detectAndMarkChords(lyrics: String, keepIndentation: Boolean = false): String {
        return lyrics.lines().joinToString(separator = "\n") { line ->
            if ('[' in line || ']' in line) {
                return@joinToString line
            }
            // inverted chords match - find expressions which are not chords
            var line2 = "]$line["
            line2 = line2.replace(Regex("""](.*?)\[""")) { matchResult ->
                "]" + markChordsInSentence(matchResult.groupValues[1], keepIndentation) + "["
            }
            line2.drop(1).dropLast(1)
        }
    }

    private fun markChordsInSentence(sentence: String, keepIndentation: Boolean): String {
        // lookahead/lookbehind regex keeping delimiter
        val words = sentence.split(Regex("""((?<= )|(?= ))"""))
        val chordsFound = mutableListOf<String>()
        val wordsIngredients = mutableListOf<String>()
        var wordsConsumed = 0
        run loop@{
            // seek chords from right to left
            words.asReversed().forEach { word ->
                if (word.isBlank()) { // whitespace
                    wordsIngredients += word
                    wordsConsumed++
                    return@forEach // continue
                }
                if (!chordParser.isWordAChord(word)) {
                    return@loop // break
                }
                wordsConsumed++
                if (keepIndentation && wordsIngredients.allFragmentsAreChords()) {
                    wordsIngredients.removeLastSpace()
                    wordsIngredients.removeLastSpace()
                }
                wordsIngredients += "[$word]"
                chordsFound += word
            }
        }
        allMarkedChords.addAll(chordsFound)
        val remainingWords = words.dropLast(wordsConsumed)
        wordsIngredients.addAll(remainingWords.asReversed())

        if (chordsFound.isEmpty())
            return sentence
        return wordsIngredients.asReversed().joinToString(separator = "")
    }

    private fun List<String>.allFragmentsAreChords(): Boolean =
        this.all { fragment ->
            fragment.isBlank() || fragment.trim().run {
                startsWith("[") && endsWith("]")
            }
        }

    private fun MutableList<String>.removeLastSpace() {
        this.removeAll { it.isEmpty() }
        this.lastOrNull()?.takeIf { it.startsWith(" ") }?.let {
            this[this.size - 1] = it.drop(1)
        }
    }

}

