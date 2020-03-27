package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.lyrics.model.LyricsTextType
import igrek.songbook.settings.theme.DisplayStyle

class LyricsArranger(
        private val displayStyle: DisplayStyle,
        private val screenWRelative: Float,
        private val lengthMapper: TypefaceLengthMapper
) {
    private val linewWrapper = LineWrapper(screenWRelative = screenWRelative,
            lengthMapper = lengthMapper)

    fun arrangeModel(model: LyricsModel): LyricsModel {
        val wrappedLines = model.lines.flatMap(this::arrangeLine)
        return LyricsModel(lines = wrappedLines)
    }

    private fun arrangeLine(line: LyricsLine): List<LyricsLine> {
        if (displayStyle == DisplayStyle.ChordsAbove) {
            return arrangeChordsAbove(line.fragments)
        }

        val fragments = preProcessFragments(line.fragments)

        if (displayStyle == DisplayStyle.ChordsInline) {
            addInlineChordsPadding(fragments)
        }

        calculateXPositions(fragments)

        val lines: List<LyricsLine> = linewWrapper.wrapLine(LyricsLine(fragments))

        return lines.onEach(this::postProcessLine)
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

    private fun arrangeChordsAbove(fragments: List<LyricsFragment>): List<LyricsLine> {
        var x = 0f
        fragments.forEach { fragment ->
            fragment.x = x
            if (fragment.type == LyricsTextType.REGULAR_TEXT) {
                x += fragment.width
            }
        }

        val lines = linewWrapper.wrapLine(LyricsLine(fragments))

        val lines2 = lines.flatMap { line ->
            val chords = LyricsLine(filterFragments(line.fragments, LyricsTextType.CHORDS))
            val texts = LyricsLine(filterFragments(line.fragments, LyricsTextType.REGULAR_TEXT))
            when {
                texts.isBlank() -> {
                    listOf(chords)
                }
                chords.isBlank() -> {
                    listOf(texts)
                }
                else -> {
                    listOf(chords, texts)
                }
            }
        }

        return lines2.onEach(this::postProcessLine)
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
        val textSpaceWidth = lengthMapper.get(LyricsTextType.REGULAR_TEXT, ' ')
        fragments.forEachIndexed { index, fragment ->
            if (fragment.type == LyricsTextType.CHORDS) {
                // previous neighbour
                fragments.getOrNull(index - 1)
                        ?.takeIf { previous -> previous.type == LyricsTextType.REGULAR_TEXT }
                        ?.takeIf { previous -> !previous.text.endsWith(" ") && !fragment.text.startsWith(" ") }
                        ?.let {
                            it.text = it.text + " "
                            it.width += textSpaceWidth
                        }
                // next neighbour
                fragments.getOrNull(index + 1)
                        ?.takeIf { next -> next.type == LyricsTextType.REGULAR_TEXT }
                        ?.takeIf { next -> !fragment.text.endsWith(" ") && !next.text.startsWith(" ") }
                        ?.let {
                            it.text = " " + it.text
                            it.width += textSpaceWidth
                        }
            }
        }
    }

    private fun chordSpaceFragment(): LyricsFragment {
        val chordSpaceWidth = lengthMapper.get(LyricsTextType.CHORDS, ' ')
        return LyricsFragment(" ", type = LyricsTextType.CHORDS, width = chordSpaceWidth)
    }

    private fun areFragmentsBlank(fragments: List<LyricsFragment>): Boolean {
        return fragments.all { fragment -> fragment.text.isBlank() }
    }

    private fun filterFragments(fragments: List<LyricsFragment>, textType: LyricsTextType): List<LyricsFragment> {
        return fragments.filter { fragment -> fragment.type == textType }
    }

}
