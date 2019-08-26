package igrek.songbook.chords.splitter

import igrek.songbook.chords.syntax.chordsAllDelimiters

class ChordsSplitter {

    /**
     * @param input         text joined with separators
     * @return a list of splitted text fragments with delimiters stored (or without if it's the last part)
     */
    fun splitWithDelimiters(input: String): List<StringWithDelimiter> {
        val splitted = mutableListOf<StringWithDelimiter>()

        // find a first delimiter
        val firstDelimiter = input.findAnyOf(chordsAllDelimiters)

        if (firstDelimiter == null) {
            splitted.add(StringWithDelimiter(input)) // the last fragment
        } else {
            val before = input.take(firstDelimiter.first)
            val after = input.drop(firstDelimiter.first + firstDelimiter.second.length)
            splitted.add(StringWithDelimiter(before, firstDelimiter.second))
            // recursive split
            splitted.addAll(splitWithDelimiters(after))
        }

        return splitted
    }

}