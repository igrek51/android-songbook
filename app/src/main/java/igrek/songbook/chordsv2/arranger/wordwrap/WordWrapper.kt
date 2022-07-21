package igrek.songbook.chordsv2.arranger.wordwrap

import igrek.songbook.chordsv2.formatter.TypefaceLengthMapper
import igrek.songbook.chordsv2.model.LyricsFragment
import igrek.songbook.chordsv2.model.LyricsLine
import igrek.songbook.chordsv2.model.LyricsTextType
import igrek.songbook.chordsv2.model.lineWrapperChar

typealias Fragment = LyricsFragment
typealias Line = LyricsLine


internal fun List<Line>.nonEmptyLines(): List<Line> =
        this.filterNot { it.isBlank }.ifEmpty { listOf(Line()) }

internal fun List<Line>.clearBlanksOnEnd(): List<Line> =
        this.dropLastWhile { it.isBlank }

internal infix fun List<Line>.zipUneven(below: List<Line>): List<Line> =
        this.zip(below).flatMap { (a, b) -> listOf(a, b) } +
                this.drop(below.size) +
                below.drop(this.size)


internal fun Line.end() =
        this.fragments.lastOrNull()?.run { x + width } ?: 0f

internal fun List<Word>.end(): Float =
        this.lastOrNull()?.end ?: 0f


internal fun List<Fragment>.toWords(lengthMapper: TypefaceLengthMapper): List<Word> =
        this.filterNot { it.text.isBlank() }
                .flatMap { it.toWords(lengthMapper) }

internal fun Fragment.toWords(lengthMapper: TypefaceLengthMapper): List<Word> {
    var baseX = this.x
    // lookahead regex keeping delimiter
    val parts = this.text.split(Regex("""(?<=[ .,\-:;/?!)])"""))
    return parts.map {
        val width = lengthMapper.stringWidth(this.type, it)
        val segment = Word(it, type = this.type, x = baseX, width = width)
        baseX += width
        segment
    }.filterNot { it.text.isEmpty() }
}

internal fun List<List<Word>>.toLines(): List<Line> =
        this.map {
            Line(it.toFragments())
        }

internal fun List<Word>.toFragments(): List<Fragment> =
        this.joinAdjacent().map {
            Fragment(it.text, it.type, it.x, it.width)
        }

internal fun List<Word>.joinAdjacent(): List<Word> =
        this.fold(mutableListOf()) { acc, segment ->
            if (acc.isEmpty() || acc.last().type != segment.type || acc.last().end != segment.x) {
                acc += segment
            } else {
                // join with last
                acc.last().text += segment.text
                acc.last().width += segment.width
            }
            acc
        }


internal fun List<Line>.addLineWrappers(screenWRelative: Float, lengthMapper: TypefaceLengthMapper): List<Line> =
        this.mapIndexed { index, line ->
            if (index < this.size - 1 && !line.isBlank) {
                Line(line.fragments + createLineWrapper(screenWRelative, lengthMapper))
            } else {
                line
            }
        }

internal fun createLineWrapper(screenWRelative: Float, lengthMapper: TypefaceLengthMapper): Fragment {
    val linewrapperW = lengthMapper.charWidth(LyricsTextType.REGULAR_TEXT, lineWrapperChar)
    val linewrapperX = screenWRelative - linewrapperW
    return Fragment(lineWrapperChar.toString(), LyricsTextType.LINEWRAPPER,
            x = linewrapperX, width = linewrapperW)
}


internal fun List<Word>.splitByXLimit(xLimit: Float): Triple<List<Word>, List<Word>, List<Word>> =
        Triple(
                this.filter { it.x <= xLimit && it.end <= xLimit },
                this.filter { it.x <= xLimit && it.end > xLimit },
                this.filter { it.x > xLimit && it.end > xLimit },
        )

internal fun alignToLeft(words: List<Word>, moveBy: Float) {
    words.forEach { char ->
        char.x -= moveBy
    }
}


