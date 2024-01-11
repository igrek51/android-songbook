package igrek.songbook.chords.arranger

import igrek.songbook.chords.arranger.wordwrap.DoubleLineWrapper
import igrek.songbook.chords.model.LyricsFragment
import igrek.songbook.chords.model.LyricsLine
import igrek.songbook.chords.model.LyricsTextType
import igrek.songbook.chords.render.TypefaceLengthMapper


class ChordsAboveArranger(
    screenWRelative: Float,
    private val lengthMapper: TypefaceLengthMapper,
    horizontalScroll: Boolean = false,
) {
    private val doubleLineWrapper = DoubleLineWrapper(
        screenWRelative = screenWRelative,
        lengthMapper = lengthMapper,
        horizontalScroll = horizontalScroll,
    )

    fun arrangeLine(line: LyricsLine): List<LyricsLine> {
        calculateXPositions(line.fragments)

        val chords = LyricsLine(
            fragments = filterFragments(line.fragments, LyricsTextType.CHORDS),
            primalIndex = line.primalIndex,
        )
        val texts = LyricsLine(
            fragments = filterFragments(
                line.fragments,
                LyricsTextType.REGULAR_TEXT, LyricsTextType.COMMENT,
            ),
            primalIndex = line.primalIndex,
        )
        preventChordsOverlapping(chords.fragments, texts.fragments)
        alignSingleChordsLeft(chords.fragments, texts.fragments)

        val lines = doubleLineWrapper.wrapDoubleLine(chords = chords, texts = texts)

        return lines.onEach(this::postProcessLine)
    }

    private fun alignSingleChordsLeft(chords: List<LyricsFragment>, texts: List<LyricsFragment>) {
        // if there's one chords section at end only, align it to left
        val textWithoutComments = texts.filter { fragment -> fragment.type == LyricsTextType.REGULAR_TEXT }
        if (!(chords.size == 1 && textWithoutComments.isNotEmpty()))
            return
        val textEnd = textWithoutComments.last().run { x + width }
        val firstChord = chords.first()
        if (textEnd <= firstChord.x) {
            firstChord.x = 0f
        }
    }

    private fun postProcessLine(line: LyricsLine): LyricsLine {
        // cleanup blank fragments
        val fragments = line.fragments
            .onEach { fragment -> fragment.text = fragment.text.trimEnd() }
            .filter { fragment -> fragment.text.isNotBlank() }

        return LyricsLine(fragments = fragments, primalIndex = line.primalIndex)
    }

    private fun calculateXPositions(fragments: List<LyricsFragment>) {
        var x = 0f
        fragments.forEach { fragment ->
            fragment.x = x
            if (fragment.type == LyricsTextType.REGULAR_TEXT || fragment.type == LyricsTextType.COMMENT) {
                x += fragment.width
            }
        }
    }

    private fun preventChordsOverlapping(
        chords: List<LyricsFragment>,
        texts: List<LyricsFragment>
    ) {
        chords.forEachIndexed { index, _ ->
            val chord = chords[index]
            chords.getOrNull(index - 1)
                ?.let { previousChord ->
                    val spaceWidth = lengthMapper.charWidth(chord.type, ' ')
                    val overlappedBy = previousChord.x + previousChord.width - chord.x + spaceWidth
                    if (overlappedBy > 0) {
                        val splitPoint = chord.x
                        // move all chords and texts to the right
                        texts.filter { fragment -> fragment.x >= splitPoint }.forEach {
                            fragment -> fragment.x += overlappedBy
                        }
                        chords.filter { fragment -> fragment.x > splitPoint }.forEach {
                            fragment -> fragment.x += overlappedBy
                        }
                        chord.x += overlappedBy
                    }
                }
        }
    }

}
