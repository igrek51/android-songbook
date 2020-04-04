package igrek.songbook.editor

import igrek.songbook.chords.detector.ChordsDetector

class ChordsMarker(
        private val detector: ChordsDetector
) {

    var allMarkedChords = mutableListOf<String>()
        private set

    fun detectAndMarkChords(lyrics: String): String {
        return lyrics.lines().joinToString(separator = "\n") { line ->
            // inverted chords match - find expressions which are not chords
            var line2 = "]$line["
            line2 = line2.replace(Regex("""](.*?)\[""")) { matchResult ->
                "]" + markChordsInSentence(matchResult.groupValues[1]) + "["
            }
            line2.drop(1).dropLast(1)
        }
    }

    private fun markChordsInSentence(sentence: String): String {
        // lookahead/lookbehind regex keeping delimiter
        val words = sentence.split(Regex("""((?<= )|(?= ))"""))
        val chordsFound = mutableListOf<String>()
        val wordsIngredients = mutableListOf<String>()
        run loop@{
            // seek chords from right to left
            words.asReversed().forEach { word ->
                if (word.isBlank()) {
                    wordsIngredients += word
                    return@forEach // continue
                }
                if (!detector.isWordAChord(word)) {
                    return@loop // break
                }
                wordsIngredients += "[$word]"
                chordsFound += word
            }
        }
        allMarkedChords.addAll(chordsFound)
        val remainingWords = words.dropLast(wordsIngredients.size).asReversed()
        wordsIngredients.addAll(remainingWords)

        if (chordsFound.isEmpty())
            return sentence
        return wordsIngredients.asReversed().joinToString(separator = "")
    }

}