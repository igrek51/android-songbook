package igrek.songbook.chords.lyrics

import android.graphics.Paint
import android.graphics.Typeface
import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.lyrics.model.LyricsTextType
import java.util.*

class LyricsWrapper(
        private val chordsEndOfLine: Boolean,
        private val chordsAbove: Boolean = true,
        screenW: Float
) {

    private var bracket: Boolean = false
    private var paint: Paint? = null
    private val wordSplitters = hashSetOf(" ", ".", ",", "-", ":", ";", "/")

    private val normalTypeface: Typeface = Typeface.create(fontFamily, Typeface.NORMAL)
    private val boldTypeface: Typeface = Typeface.create(fontFamily, Typeface.BOLD)

    private var boldSpaceWidth = 0f

    @Synchronized
    fun parseFileContent(model: LyricsModel): LyricsModel {
        val model = LyricsModel()

        return model
    }

    private fun parseLine(line: String, screenW: Float, fontsize: Float): List<LyricsLine> {
        var chars: List<LyricsChar> = str2chars(line)
        if (chordsEndOfLine)
            chars = moveCharChordsAtTheEndOfLine(chars)
        val lines2: List<List<LyricsChar>> = wrapLine(chars, screenW)

        val lines: MutableList<LyricsLine> = mutableListOf()
        for (subline in lines2) {
            val lyricsLine = chars2line(subline, fontsize)
            if (chordsEndOfLine)
                moveChordsAtTheEndOfLine(lyricsLine, screenW, fontsize)
            lines.add(lyricsLine)
        }

        if (chordsAbove) {
            return extractChordsAbove(lines)
        }

        return lines
    }

    private fun extractChordsAbove(lines: MutableList<LyricsLine>): MutableList<LyricsLine> {
        val splitLines: MutableList<LyricsLine> = mutableListOf()
        lines.forEach { line ->
            val chords: LyricsLine = filterLineByTextType(line, LyricsTextType.CHORDS, LyricsTextType.BRACKET)
            val texts: LyricsLine = removeChordsFromText(line)

            when {
                texts.isBlank() -> {
                    splitLines.add(chords)
                }
                chords.isBlank() -> {
                    splitLines.add(texts)
                }
                else -> {
                    splitLines.add(chords)
                    splitLines.add(texts)
                }
            }
        }
        return splitLines
    }

    private fun removeChordsFromText(line: LyricsLine): LyricsLine {
        val movedTextFragments: MutableList<LyricsFragment> = mutableListOf()
        line.fragments.forEachIndexed { index, fragment ->
            when (fragment.type) {
                LyricsTextType.LINEWRAPPER, LyricsTextType.REGULAR_TEXT -> {
                    movedTextFragments.add(fragment)
                }
                LyricsTextType.CHORDS -> {
                    // move back consecutive text parts
                    line.fragments.drop(index + 1)
                            .filter { consecutiveFragment ->
                                consecutiveFragment.type == LyricsTextType.REGULAR_TEXT
                            }.forEach { consecutiveFragment ->
                                consecutiveFragment.x -= fragment.width
                            }
                }
            }
        }
        return LyricsLine(line.y, movedTextFragments)
    }

    private fun filterLineByTextType(line: LyricsLine, vararg allowedTypes: LyricsTextType): LyricsLine {
        val fragments = line.fragments.filter { fragment ->
            fragment.type in allowedTypes
        }.toMutableList()
        return LyricsLine(line.y, fragments)
    }

    private fun moveCharChordsAtTheEndOfLine(chars: List<LyricsChar>): List<LyricsChar> {
        val nonChords = chars.filter { lyricsChar -> lyricsChar.type != LyricsTextType.CHORDS }
        // add dividers between alone chords
        var last: LyricsChar? = null
        chars.forEach { c ->
            // closing bracket
            if (last?.type == LyricsTextType.BRACKET && c.type == LyricsTextType.CHORDS) {
                c.width += boldSpaceWidth
                c.c = " " + c.c
            }
            last = c
        }
        val chordChars = chars.filter { lyricsChar -> lyricsChar.type == LyricsTextType.CHORDS }
        return nonChords + chordChars
    }

    private fun moveChordsAtTheEndOfLine(lyricsLine: LyricsLine, screenW: Float, fontsize: Float) {
        // recalculate fragments positions
        val lastFragment = lyricsLine.fragments.reversed().firstOrNull() ?: return

        val moveRight = screenW / fontsize - (lastFragment.x + lastFragment.width)
        lyricsLine.fragments.forEach { fragment ->
            if (fragment.type == LyricsTextType.CHORDS) {
                fragment.x += moveRight
            }
        }
    }

    private fun str2chars(line: String): List<LyricsChar> {
        val chars = ArrayList<LyricsChar>()
        for (element in line) {
            val c = element.toString()

            val charWidth: Float
            val type: LyricsTextType

            when (c) {
                "[" -> {
                    setBracket(true)
                    charWidth = 0f
                    type = LyricsTextType.BRACKET
                }
                "]" -> {
                    setBracket(false)
                    charWidth = 0f
                    type = LyricsTextType.BRACKET
                }
                else -> {
                    val fw = FloatArray(1)
                    paint!!.getTextWidths(c, fw)
                    charWidth = fw[0]
                    type = if (bracket) {
                        LyricsTextType.CHORDS
                    } else {
                        LyricsTextType.REGULAR_TEXT
                    }
                }
            }

            chars.add(LyricsChar(c, charWidth, type))
        }
        return chars
    }


    private fun textWidth(chars: List<LyricsChar>): Float {
        var sum = 0f
        for (achar in chars) {
            sum += achar.width
        }
        return sum
    }

    private fun maxScreenStringLength(chars: List<LyricsChar>, screenW: Float): Int {
        var l = chars.size
        while (textWidth(chars.subList(0, l)) > screenW && l > 1) {
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
        while (--toIndex > 1) {
            // is word splitter
            if (wordSplitters.contains(chars[toIndex].c))
                return toIndex
        }
        return -1
    }

    private fun wrapLine(chars: List<LyricsChar>, screenW: Float): List<List<LyricsChar>> {
        val lines = ArrayList<List<LyricsChar>>()
        if (textWidth(chars) <= screenW) {
            lines.add(chars)
        } else {
            val maxLength = maxScreenStringLength(chars, screenW)
            val before = chars.subList(0, maxLength)
            // copying due to subsequent modifications
            val newBefore = ArrayList(before)
            // add special line wrapper
            newBefore.add(LyricsChar("\u21B5", 0f, LyricsTextType.LINEWRAPPER))
            val after = chars.subList(maxLength, chars.size)
            lines.add(newBefore)
            lines.addAll(wrapLine(after, screenW))
        }
        return lines
    }

    @Synchronized
    private fun chars2line(chars: List<LyricsChar>, fontsize: Float): LyricsLine {
        // aggregate groups of the same type
        val line = LyricsLine()

        var lastType: LyricsTextType? = null
        var buffer = StringBuilder()
        var startX = 0f
        var x = 0f
        for (i in chars.indices) {
            val lyricsChar = chars[i]
            if (lastType == null)
                lastType = lyricsChar.type

            if (lyricsChar.type != lastType) {
                // complete the previous fragment
                if (buffer.isNotEmpty()) {

                    if (lastType.isDisplayable) {
                        val fragmentWidth = x - startX
                        val fragment = LyricsFragment(
                                startX / fontsize,
                                buffer.toString(),
                                lastType,
                                fragmentWidth / fontsize)
                        line.addFragment(fragment)
                    }

                    startX = x
                    buffer = StringBuilder()
                }

                lastType = lyricsChar.type
            }

            if (lyricsChar.type.isDisplayable) {
                buffer.append(lyricsChar.c)
                x += lyricsChar.width
            }
        }

        // complete the last fragment
        if (buffer.isNotEmpty()) {
            if (lastType != null && lastType.isDisplayable) {
                val fragmentWidth = x - startX
                val fragment = LyricsFragment(startX / fontsize,
                        buffer.toString(),
                        lastType,
                        fragmentWidth / fontsize)
                line.addFragment(fragment)
            }
        }

        return line
    }

}
