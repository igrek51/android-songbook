package igrek.songbook.chords.lyrics.wrapper

import igrek.songbook.chords.lyrics.TypefaceLengthMapper
import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsTextType
import igrek.songbook.chords.lyrics.model.lineWrapperChar

typealias Fragment = LyricsFragment
typealias Line = LyricsLine

class DoubleLineWrapper(
        private val screenWRelative: Float,
        private val lengthMapper: TypefaceLengthMapper
) {

    fun wrapDoubleLine(chords: Line, texts: Line): List<Line> {
        if (texts.end() <= screenWRelative && chords.end() <= screenWRelative) {
            return listOf(chords, texts).nonEmptyLines()
        }

        val chordSegments: List<Segment> = chords.fragments.toSegments()
        val textSegments: List<Segment> = texts.fragments.toSegments()

        val (wrappedChordSegments, wrappedTextSegments) = wrapDoubleSegments(chordSegments, textSegments)

        val chordLines: List<Line> = wrappedChordSegments.toLines().clearBlanksOnEnd().addLineWrappers()
        val textLines: List<Line> = wrappedTextSegments.toLines().clearBlanksOnEnd().addLineWrappers()
        val lines = chordLines zipUneven textLines

        return lines.nonEmptyLines()
    }

    private fun List<Line>.nonEmptyLines(): List<Line> =
            this.filterNot { it.isBlank() }.ifEmpty { listOf(Line()) }

    private fun List<Line>.clearBlanksOnEnd(): List<Line> =
            this.dropLastWhile { it.isBlank() }

    private infix fun List<Line>.zipUneven(below: List<Line>): List<Line> =
            this.zip(below).flatMap { (a, b) -> listOf(a, b) } +
                    this.drop(below.size) +
                    below.drop(this.size)

    private fun List<Fragment>.toSegments(): List<Segment> =
            this.filterNot { it.text.isBlank() }
                    .flatMap { it.toSegments() }

    private fun List<List<Segment>>.toLines(): List<Line> =
            this.map {
                Line(it.toFragments())
            }

    private fun List<Segment>.toFragments(): List<Fragment> =
            this.joinAdjacent().map {
                Fragment(it.text, it.type, it.x, it.width)
            }

    private fun List<Segment>.joinAdjacent(): List<Segment> =
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

    private fun List<Line>.addLineWrappers(): List<Line> =
            this.mapIndexed { index, line ->
                if (index < this.size - 1 && !line.isBlank()) {
                    Line(line.fragments + createLineWrapper())
                } else {
                    line
                }
            }

    private fun Fragment.toSegments(): List<Segment> {
        var baseX = this.x
        val parts = this.text.split(Regex("""(?<=[ .,\-:;/?!)])"""))
        return parts.map {
            val width = lengthMapper.stringWidth(this.type, it)
            val segment = Segment(it, type = this.type, x = baseX, width = width)
            baseX += width
            segment
        }.filterNot { it.text.isEmpty() }
    }

    private fun Line.end(): Float {
        return this.fragments.lastOrNull()?.run { x + width } ?: 0f
    }

    private fun List<Segment>.end(): Float =
            this.lastOrNull()?.end ?: 0f

    private fun List<Segment>.splitByXLimit(xLimit: Float): Pair<List<Segment>, List<Segment>> =
            this.partition { it.end <= xLimit }

    private fun List<Segment>.splitOnThree(xLimit: Float): Triple<List<Segment>, List<Segment>, List<Segment>> =
            Triple(
                    this.filter { it.x <= xLimit && it.end <= xLimit },
                    this.filter { it.x <= xLimit && it.end > xLimit },
                    this.filter { it.x > xLimit && it.end > xLimit },
            )

    private fun List<Segment>.takeAndDrop(position: Int): Pair<List<Segment>, List<Segment>> =
            this.take(position) to this.drop(position)

    private fun wrapDoubleSegments(chords: List<Segment>, texts: List<Segment>): Pair<List<List<Segment>>, List<List<Segment>>> {
        when {
            chords.end() <= screenWRelative && texts.end() <= screenWRelative -> return listOf(chords) to listOf(texts)
            chords.end() <= screenWRelative -> return wrapSingleSegments(chords) to wrapSingleSegments(texts)
            texts.end() <= screenWRelative -> return wrapSingleSegments(chords) to wrapSingleSegments(texts)
        }

//        val maxChords = fittingSegmentsCount(chords)
//        val maxTexts = fittingSegmentsCount(texts)
//
//        val (beforeChords, afterChords) = chords.takeAndDrop(maxChords)
//        val (beforeTexts, afterTexts) = texts.takeAndDrop(maxTexts)

        val (beforeC, middleC, afterC) = chords.splitOnThree(screenWRelative)
        val (beforeT, middleT, afterT) = texts.splitOnThree(screenWRelative)

        val toWrapC = middleC + afterC
        val toWrapT = middleT + afterT

        val moveByC = toWrapC.firstOrNull()?.x ?: 0f
        val moveByT = toWrapT.firstOrNull()?.x ?: 0f

        // very long segment, cant be wrapped
        if (moveByC <= 0f)
            return listOf(chords) to wrapSingleSegments(texts)
        if (moveByT <= 0f)
            return wrapSingleSegments(chords) to listOf(texts)

        if (toWrapC.isEmpty())
            return listOf(chords) to wrapSingleSegments(texts)
        if (toWrapT.isEmpty())
            return wrapSingleSegments(chords) to listOf(texts)

        val moveBy = listOf(moveByC, moveByT).min() ?: 0f

        if (moveBy <= 0f)
            return wrapSingleSegments(chords) to wrapSingleSegments(texts)

//            listOfNotNull(
//                afterChords.firstOrNull(),
//                afterTexts.firstOrNull(),
//        ).map { it.x }.min() ?: screenWRelative

        alignToLeft(toWrapC, moveBy)
        alignToLeft(toWrapT, moveBy)

//        if (moveBy <= 0) {
//            when {
//                maxChords == 0 && maxTexts == 0 -> return listOf(chords) to listOf(texts)
//                maxChords == 0 -> return listOf(chords) to wrapSingleSegments(texts)
//                maxTexts == 0 -> return wrapSingleSegments(chords) to listOf(texts)
//            }
//        }

        val (nextC, nextT) = wrapDoubleSegments(toWrapC, toWrapT)

        val allWrappedChords = listOf(beforeC) + nextC
        val allWrappedTexts = listOf(beforeT) + nextT

        return allWrappedChords to allWrappedTexts
    }

    private fun wrapSingleSegments(segments: List<Segment>): List<List<Segment>> {
        if (segments.isEmpty())
            return emptyList()
        if (segments.end() <= screenWRelative)
            return listOf(segments)

//        val maxSegments = fittingSegmentsCount(segments)

        val (before, middle, after) = segments.splitOnThree(screenWRelative)

        val toWrap = middle + after
        val moveBy = toWrap.firstOrNull()?.x ?: 0f

        if (toWrap.isEmpty())
            return listOf(segments)
        if (moveBy <= 0f) // very long segment, cant be wrapped
            return listOf(segments)

//        val before = segments.take(maxSegments)
//        val after = segments.drop(maxSegments)
//        val moveBy = after.firstOrNull()?.x ?: 0f

        alignToLeft(toWrap, moveBy)

        return listOf(before) + wrapSingleSegments(toWrap)
    }

    private fun createLineWrapper(): Fragment {
        val linewrapperW = lengthMapper.charWidth(LyricsTextType.REGULAR_TEXT, lineWrapperChar)
        val linewrapperX = screenWRelative - linewrapperW
        return Fragment(lineWrapperChar.toString(), LyricsTextType.LINEWRAPPER,
                x = linewrapperX, width = linewrapperW)
    }

    private fun fittingSegmentsCount(segments: List<Segment>): Int {
        if (segments.end() <= screenWRelative)
            return segments.size

        return bestSegmentSequenceLength(segments)
    }

    private fun alignToLeft(segments: List<Segment>, moveBy: Float) {
        segments.forEach { char ->
            char.x -= moveBy
        }
    }

    private fun bestSegmentSequenceLength(segments: List<Segment>): Int {
        return bisectLongestRange(
                evaluator = { index -> segments.getOrNull(index - 1)?.end ?: 0f },
                maxCarriage = segments.size,
                valueLimit = screenWRelative
        )
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
}
