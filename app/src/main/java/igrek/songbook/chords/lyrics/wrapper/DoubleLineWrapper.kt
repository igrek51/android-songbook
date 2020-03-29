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

        val chordLines: List<Line> = wrappedChordSegments.toLines().addLineWrappers()
        val textLines: List<Line> = wrappedTextSegments.toLines().addLineWrappers()
        val lines = chordLines zipUneven textLines

        return lines.nonEmptyLines()
    }

    private fun List<Line>.nonEmptyLines(): List<Line> =
            this.filterNot { it.isBlank() }.ifEmpty { listOf(Line()) }

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
        }
    }

    private fun Line.end(): Float {
        return this.fragments.lastOrNull()?.run { x + width } ?: 0f
    }

    private fun List<Segment>.end(): Float =
            this.lastOrNull()?.end ?: 0f

    private fun List<Segment>.splitByXLimit(xLimit: Float): Pair<List<Segment>, List<Segment>> =
            this.partition { it.end <= xLimit }

    private fun wrapDoubleSegments(chords: List<Segment>, texts: List<Segment>): Pair<List<List<Segment>>, List<List<Segment>>> {
        when {
            chords.isEmpty() && texts.isEmpty() -> return emptyList<List<Segment>>() to emptyList()
            chords.isEmpty() -> return emptyList<List<Segment>>() to wrapSingleSegments(texts)
            texts.isEmpty() -> return wrapSingleSegments(chords) to emptyList()
        }

        val maxChords = fittingSegmentsCount(chords)
        val maxTexts = fittingSegmentsCount(texts)
        val xLimit = listOfNotNull(
                chords.take(maxChords),
                texts.take(maxTexts),
        ).mapNotNull { it.lastOrNull()?.end }.max() ?: screenWRelative

        val (beforeChords, afterChords) = chords.splitByXLimit(xLimit)
        val (beforeTexts, afterTexts) = texts.splitByXLimit(xLimit)

        alignToLeft(afterChords, xLimit)
        alignToLeft(afterTexts, xLimit)

        val (nextChordWraps, nextTextWraps) = wrapDoubleSegments(afterChords, afterTexts)

        val allWrappedChords = listOf(beforeChords) + nextChordWraps
        val allWrappedTexts = listOf(beforeTexts) + nextTextWraps

        return allWrappedChords to allWrappedTexts
    }

    private fun wrapSingleSegments(segments: List<Segment>): List<List<Segment>> {
        if (segments.end() <= screenWRelative) {
            return listOf(segments)
        }

        val maxSegments = fittingSegmentsCount(segments)
        val before = segments.take(maxSegments)
        val after = segments.drop(maxSegments)

        alignToLeft(after, moveBy = after.firstOrNull()?.x ?: 0f)

        return listOf(before) + wrapSingleSegments(after)
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
