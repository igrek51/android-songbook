package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsChar
import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsTextType

class LineWrapper(
        private val screenWEm: Float,
        private val normalCharLengths: Map<Char, Float>,
        private val boldCharLengths: Map<Char, Float>
) {

    private val wordSplitters = hashSetOf(' ', '.', ',', '-', ':', ';', '/', '?', '!', ')')

    fun wrapLine(line: LyricsLine): List<LyricsLine> {
        if (fragmentsWidth(line.fragments) <= screenWEm) {
            return listOf(line)
        }

        val chars = line.fragments.flatMap(LyricsChar.Companion::fromLyricsFragment)
        chars.forEach { char ->
            char.widthEm = when (char.type) {
                LyricsTextType.REGULAR_TEXT -> normalCharLengths[char.c]
                LyricsTextType.CHORDS -> boldCharLengths[char.c]
                else -> boldCharLengths[' ']
            } ?: 0f
        }

        val wrappedChars = wrapChars(chars)

        return wrappedChars.map { lineChars ->
            val fragments = lineChars
                    .groupBy { char -> char.type }
                    .map { (type, fragmentChars) ->
                        val text = fragmentChars.map { char -> char.c }.joinToString(separator = "")
                        val width = fragmentChars.map { char -> char.widthEm }.sum()
                        LyricsFragment(text = text, type = type, widthEm = width)
                    }
            LyricsLine(fragments)
        }
    }

    private fun wrapChars(chars: List<LyricsChar>): List<List<LyricsChar>> {
        if (charsWidth(chars) <= screenWEm) {
            return listOf(chars)
        }

        val maxLength = maxScreenStringLength(chars)
        val before = chars.take(maxLength)
        val after = chars.subList(maxLength, chars.size)
        val linewrapper = LyricsChar('\u21B5', type = LyricsTextType.LINEWRAPPER, widthEm = 0f)

        return listOf(before + linewrapper) + wrapChars(after)
    }

    private fun charsWidth(chars: List<LyricsChar>): Float {
        return chars.map { char -> char.widthEm }.sum()
    }

    private fun fragmentsWidth(fragments: List<LyricsFragment>): Float {
        return fragments.map { fragment -> fragment.widthEm }.sum()
    }

    private fun maxScreenStringLength(chars: List<LyricsChar>): Int {
        var l = chars.size
        while (charsWidth(chars.subList(0, l)) > screenWEm && l > 1) {
            l--
        }
        // do not wrap in the middle of the word, try to step back until word splitter found
        return when (val lastWordSplitter = findLastWordSplitter(chars, l)) {
            -1 -> l// it's one long word only - no way to split
            l - 1 -> l// last char is already a word splitter
            else -> lastWordSplitter + 1 // split after last word splitter
        }
    }

    private fun findLastWordSplitter(chars: List<LyricsChar>, _toIndex: Int): Int {
        var toIndex = _toIndex
        while (--toIndex >= 0) {
            // text - chord frontier
            if (toIndex + 1 < chars.size && chars[toIndex].type != chars[toIndex + 1].type)
                return toIndex
            // is word splitter
            if (chars[toIndex].c in wordSplitters)
                return toIndex
        }
        return -1
    }
}