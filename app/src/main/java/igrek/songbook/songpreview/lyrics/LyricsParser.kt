package igrek.songbook.songpreview.lyrics

import android.graphics.Paint
import android.graphics.Typeface
import java.util.*

class LyricsParser(fontFamily: Typeface, private val chordsEndOfLine: Boolean) {

    private var bracket: Boolean = false
    private var paint: Paint? = null
    private val wordSplitters = hashSetOf(" ", ".", ",", "-", ":", ";", "/")

    private val normalTypeface: Typeface = Typeface.create(fontFamily, Typeface.NORMAL)
    private val boldTypeface: Typeface = Typeface.create(fontFamily, Typeface.BOLD)

    private val chordsEndlineRegex = """^(.*?)(\[.+?])(.*?)$""".toRegex()
    private val chordsOpenerEndlineRegex = """^(.*?)(\[.*?)$""".toRegex()
    private val chordsCloserEndlineRegex = """^(.*])(.*?)$""".toRegex()

    @Synchronized
    fun parseFileContent(content: String, screenW: Float, fontsize: Float, paint: Paint): LyricsModel {
        this.paint = paint
        setNormalFont()
        paint.textSize = fontsize

        val model = LyricsModel()
        setBracket(false)
        val lines1 = content.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (line1 in lines1) {
            model.addLines(parseLine(line1, screenW, fontsize))
        }

        // store line numbers
        for ((y, line) in model.lines.withIndex()) {
            line.y = y
        }

        return model
    }

    private fun parseLine(_line: String, screenW: Float, fontsize: Float): List<LyricsLine> {
        val line = if (chordsEndOfLine) {
            regexChordsAtTheEndOfLine(_line)
        } else {
            _line
        }
        val chars: List<LyricsChar> = str2chars(line)
        val lines2: List<List<LyricsChar>> = wrapLine(chars, screenW)

        val lines: MutableList<LyricsLine> = mutableListOf()
        for (subline in lines2) {
            val lyricsLine = chars2line(subline, fontsize)
            if (chordsEndOfLine)
                moveChordsAtTheEndOfLine(lyricsLine, screenW, fontsize)
            lines.add(lyricsLine)
        }

        return lines
    }

    private fun regexChordsAtTheEndOfLine(line: String): String {
        var replaced = line
        val chords = mutableListOf<String>()

        while (true) {
            val matchResult = chordsEndlineRegex.find(replaced) ?: break
            replaced = "${matchResult.groupValues[1]}${matchResult.groupValues[3]}"
            chords.add(matchResult.groupValues[2])
        }

        var matchResult = chordsOpenerEndlineRegex.find(replaced)
        if (matchResult != null) {
            replaced = matchResult.groupValues[1]
            chords.add(matchResult.groupValues[2])
        }
        matchResult = chordsCloserEndlineRegex.find(replaced)
        if (matchResult != null) {
            replaced = matchResult.groupValues[2]
            chords.add(0, matchResult.groupValues[1])
        }

        return if (chords.isEmpty()) {
            replaced
        } else {
            """$replaced ${chords.joinToString(separator = " ")}"""
        }
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
        for (i in 0 until line.length) {
            val c = line[i].toString()

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


    private fun setBracket(bracket: Boolean) {
        this.bracket = bracket
        // change typeface due to text width calculation
        if (bracket) {
            setBoldFont()
        } else {
            setNormalFont()
        }
    }

    private fun setNormalFont() {
        paint!!.typeface = normalTypeface
    }

    private fun setBoldFont() {
        paint!!.typeface = boldTypeface
    }
}
