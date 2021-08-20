package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsTextType
import igrek.songbook.chords.lyrics.wrapper.DoubleLineWrapper

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

        val chords = LyricsLine(filterFragments(line.fragments, LyricsTextType.CHORDS))
        val texts = LyricsLine(
            filterFragments(
                line.fragments,
                LyricsTextType.REGULAR_TEXT,
                LyricsTextType.COMMENT
            )
        )
        preventChordsOverlapping(chords.fragments, texts.fragments)
        alignSingleChordsLeft(chords.fragments, texts.fragments)

        val lines = doubleLineWrapper.wrapDoubleLine(chords = chords, texts = texts)

        return lines.onEach(this::postProcessLine)
    }

    private fun alignSingleChordsLeft(chords: List<LyricsFragment>, texts: List<LyricsFragment>) {
        // if there's one chords section at end only, align it to left
        if (!(chords.size == 1 && texts.isNotEmpty()))
            return
        val textEnd = texts.last().run { x + width }
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

        return LyricsLine(fragments)
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

    private fun preventChordsOverlapping(chords: List<LyricsFragment>, texts: List<LyricsFragment>) {
        chords.forEachIndexed { index, chord ->
            chords.getOrNull(index - 1)
                    ?.let {
                        val spaceWidth = lengthMapper.charWidth(chord.type, ' ')
                        val overlappedBy = it.x + it.width - chord.x
                        if (overlappedBy > 0) {
                            texts.filter { f -> f.x > chord.x }.forEach { f -> f.x += overlappedBy }
                            chord.x += overlappedBy + spaceWidth
                        }
                    }
        }
    }

}
