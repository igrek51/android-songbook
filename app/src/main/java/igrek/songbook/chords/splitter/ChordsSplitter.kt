package igrek.songbook.chords.splitter

class ChordsSplitter {

    companion object {
        val delimiters = listOf(" ", "-", "(", ")", "/", ",", "\n")
    }

    /**
     * @param input         text joined with separators
     * @return a list of splitted text fragments with delimiters stored (or without if it's the last part)
     */
    fun splitWithDelimiters(input: String): List<StringWithDelimiter> {
        val splitted = mutableListOf<StringWithDelimiter>()

        // find a first delimiter
        val firstDelimiter = input.findAnyOf(delimiters)

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