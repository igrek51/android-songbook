package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.*

class LineWrapper(
        private val screenWRelative: Float,
        private val lengthMapper: TypefaceLengthMapper
) {

    private val wordSplitters = hashSetOf(' ', '.', ',', '-', ':', ';', '/', '?', '!', ')')

    fun wrapLine(line: LyricsLine): List<LyricsLine> {
        if (fragmentsWidth(line.fragments) <= screenWRelative) {
            return listOf(line)
        }

        val chars = fragmentsToChars(line.fragments)

        val wrappedChars = wrapChars(chars)

        return charsToLines(wrappedChars)
    }

    private fun fragmentsToChars(fragments: List<LyricsFragment>): List<LyricsChar> {
        return fragments.flatMap(this::fragmentToChars)
    }

    private fun fragmentToChars(fragment: LyricsFragment): List<LyricsChar> {
        var previousWidths = 0f
        return fragment.text.map { char ->
            val x = fragment.x + previousWidths
            val width = lengthMapper.get(fragment.type, char)
            previousWidths += width
            LyricsChar(c = char, type = fragment.type, x = x, width = width)
        }
    }

    private fun charsToLines(wrappedChars: List<List<LyricsChar>>): List<LyricsLine> {
        return wrappedChars.map { lineChars ->
            val fragments = lineChars
                    .groupBy { char -> char.type }
                    .map { (type, fragmentChars) ->
                        val text = fragmentChars.map { char -> char.c }.joinToString(separator = "")
                        val x = fragmentChars.first().x
                        val width = fragmentChars.map { char -> char.width }.sum()
                        LyricsFragment(text = text, type = type, x = x, width = width)
                    }
            LyricsLine(fragments)
        }
    }

    private fun wrapChars(chars: List<LyricsChar>): List<List<LyricsChar>> {
        if (charsWidth(chars) <= screenWRelative) {
            return listOf(chars)
        }

        val maxLength = maxScreenStringLength(chars)
        val before = chars.take(maxLength)
        val after = chars.drop(maxLength)
        alignToLeft(after)

        val linewrapperW = lengthMapper.get(LyricsTextType.REGULAR_TEXT, lineWrapperChar)
        val linewrapperX = screenWRelative - linewrapperW
        val linewrapper = LyricsChar(lineWrapperChar, type = LyricsTextType.LINEWRAPPER,
                x = linewrapperX, width = linewrapperW)

        return listOf(before + linewrapper) + wrapChars(after)
    }

    private fun alignToLeft(chars: List<LyricsChar>) {
        val moveBy = chars.firstOrNull()?.x ?: 0f
        chars.forEach { char ->
            char.x -= moveBy
        }
    }

    private fun charsWidth(chars: List<LyricsChar>): Float {
        return chars.map { char -> char.width }.sum()
    }

    private fun fragmentsWidth(fragments: List<LyricsFragment>): Float {
        return fragments.map { fragment -> fragment.width }.sum()
    }

    private fun maxScreenStringLength(chars: List<LyricsChar>): Int {
        val carriage = bisectLongestRange(
                evaluator = { index -> charsWidth(chars.subList(0, index)) },
                maxCarriage = chars.size,
                valueLimit = screenWRelative
        )
        // do not wrap in the middle of the word, try to step back until word splitter found
        return when (val lastWordSplitter = findLastWordSplitter(chars, carriage)) {
            -1 -> carriage// it's one long word only - no way to split
            carriage - 1 -> carriage// last char is already a word splitter
            else -> lastWordSplitter + 1 // split after last word splitter
        }
    }

    private fun bisectLongestRange(evaluator: (Int) -> Float, maxCarriage: Int, valueLimit: Float): Int {
        var leftLimit = 0
        var rightLimit = maxCarriage
        while (leftLimit + 1 < rightLimit) {
            val carriage = (rightLimit - leftLimit) / 2 + leftLimit
            if (evaluator(carriage) > valueLimit) {
                rightLimit = carriage
            } else {
                leftLimit = carriage
            }
        }
        return leftLimit
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