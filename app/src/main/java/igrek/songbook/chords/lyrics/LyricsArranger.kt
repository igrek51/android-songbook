package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.lyrics.model.LyricsTextType
import igrek.songbook.chords.lyrics.wrapper.Fragment
import igrek.songbook.chords.lyrics.wrapper.LineWrapper
import igrek.songbook.settings.theme.DisplayStyle

class LyricsArranger(
        private val displayStyle: DisplayStyle,
        private val screenWRelative: Float,
        private val lengthMapper: TypefaceLengthMapper
) {
    private val lineWrapper = LineWrapper(
            screenWRelative = screenWRelative,
            lengthMapper = lengthMapper,
    )

    fun arrangeModel(model: LyricsModel): LyricsModel {
        val arrangerStrategy = when (displayStyle) {
            DisplayStyle.ChordsAbove -> {
                val arranger = ChordsAboveArranger(screenWRelative, lengthMapper)
                arranger::arrangeLine
            }
            else -> this::arrangeLine
        }
        val lines = model.lines.flatMap(arrangerStrategy)
        return LyricsModel(lines = lines)
    }

    private fun arrangeLine(line: LyricsLine): List<LyricsLine> {
        val fragments = preProcessFragments(line.fragments)

        addInlineChordsPadding(fragments)

        calculateXPositions(fragments)

        val lines: List<LyricsLine> = lineWrapper.wrapLine(LyricsLine(fragments))

        return lines.map(this::postProcessLine)
    }

    private fun preProcessFragments(fragments: List<LyricsFragment>): List<LyricsFragment> {
        return when (displayStyle) {
            DisplayStyle.ChordsOnly -> filterFragments(fragments, LyricsTextType.CHORDS)
            DisplayStyle.LyricsOnly -> filterFragments(fragments, LyricsTextType.REGULAR_TEXT)
            DisplayStyle.ChordsAlignedRight -> {
                val chords = filterFragments(fragments, LyricsTextType.CHORDS)
                val texts = filterFragments(fragments, LyricsTextType.REGULAR_TEXT)
                if (areFragmentsBlank(chords)) {
                    texts
                } else {
                    texts + chords + chordSpaceFragment()
                }
            }
            else -> fragments
        }
    }

    private fun postProcessLine(line: LyricsLine): LyricsLine {
        if (displayStyle == DisplayStyle.ChordsAlignedRight) {
            alignChordsRight(line)
        }

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
            x += fragment.width
        }
    }

    private fun alignChordsRight(line: LyricsLine) {
        val chords = filterFragments(line.fragments, LyricsTextType.CHORDS)
        val lastChord = chords.lastOrNull()
        if (lastChord != null) {
            val moveRightBy = screenWRelative - (lastChord.x + lastChord.width)
            chords.forEach { chordFragment ->
                chordFragment.x += moveRightBy
            }
        }
    }

    private fun addInlineChordsPadding(fragments: List<LyricsFragment>) {
        val textSpaceWidth = lengthMapper.charWidth(LyricsTextType.REGULAR_TEXT, ' ')
        fragments.forEachIndexed { index, fragment ->
            if (fragment.type == LyricsTextType.CHORDS) {
                val previous = fragments.getOrNull(index - 1)
                val next = fragments.getOrNull(index + 1)
                padInlineChord(previous, fragment, next, textSpaceWidth)
            }
        }
    }

    private fun padInlineChord(previous: Fragment?, current: Fragment, next: Fragment?, textSpaceWidth: Float) {
        // when chord inside a word, skip separating
        if (previous.touchesWithNext(current) && current.touchesWithNext(next) &&
                previous?.type == LyricsTextType.REGULAR_TEXT && next?.type == LyricsTextType.REGULAR_TEXT) {
            return
        }

        previous?.takeIf { previous.touchesWithNext(current) }
                ?.let {
                    it.text = it.text + " "
                    it.width += textSpaceWidth
                }

        next?.takeIf { current.touchesWithNext(next) }
                ?.let {
                    it.text = " " + it.text
                    it.width += textSpaceWidth
                }
    }

    private fun Fragment?.touchesWithNext(next: Fragment?): Boolean {
        if (this == null || next == null)
            return false
        return !this.text.endsWith(" ") && !next.text.startsWith(" ")
    }

    private fun chordSpaceFragment(): LyricsFragment {
        val chordSpaceWidth = lengthMapper.charWidth(LyricsTextType.CHORDS, ' ')
        return LyricsFragment(" ", type = LyricsTextType.CHORDS, width = chordSpaceWidth)
    }

    private fun areFragmentsBlank(fragments: List<LyricsFragment>): Boolean {
        return fragments.all { fragment -> fragment.text.isBlank() }
    }

}

internal fun filterFragments(fragments: List<LyricsFragment>, textType: LyricsTextType): List<LyricsFragment> {
    return fragments.filter { fragment -> fragment.type == textType }
}
