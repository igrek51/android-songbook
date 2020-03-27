package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.lyrics.model.LyricsTextType
import igrek.songbook.settings.theme.DisplayStyle

class LyricsArranger(
        private val displayStyle: DisplayStyle,
        screenWRelative: Float,
        private val lengthMapper: TypefaceLengthMapper
) {
    private val linewWrapper = LineWrapper(screenWRelative = screenWRelative,
            lengthMapper = lengthMapper)

    fun arrangeModel(model: LyricsModel): LyricsModel {
        val wrappedLines = model.lines.flatMap(this::arrangeLine)
        return LyricsModel(lines = wrappedLines)
    }

    private fun arrangeLine(line: LyricsLine): List<LyricsLine> {
        val chords = filterFragments(line.fragments, LyricsTextType.CHORDS)
        val texts = filterFragments(line.fragments, LyricsTextType.REGULAR_TEXT)

        var fragments = when (displayStyle) {
            DisplayStyle.ChordsOnly -> chords
            DisplayStyle.LyricsOnly -> texts
            DisplayStyle.ChordsAlignedRight -> {
                if (areFragmentsBlank(chords)) {
                    texts
                } else {
                    texts + chords + chordSpaceFragment()
                }
            }
            else -> line.fragments
        }

        if (displayStyle == DisplayStyle.ChordsAbove) {

            var x = 0f
            fragments.forEach { fragment ->
                fragment.x = x
                if (fragment.type == LyricsTextType.REGULAR_TEXT) {
                    x += fragment.width
                }
            }

        } else {

            var x = 0f
            fragments.forEach { fragment ->
                fragment.x = x
                x += fragment.width
            }

        }

        // TODO add margins to inline chords

        val lines = linewWrapper.wrapLine(LyricsLine(fragments))


        if (displayStyle == DisplayStyle.ChordsAbove) {
            return extractChordsAbove(lines)
        }

        if (displayStyle == DisplayStyle.ChordsAlignedRight) {
            lines.forEach { line ->
//                val lastFragment = line.fragments.reversed().firstOrNull()

//                val moveRight = screenW / fontsize - (lastFragment.x + lastFragment.width)
//                line.fragments.forEach { fragment ->
//                    if (fragment.type == LyricsTextType.CHORDS) {
//                        fragment.x += moveRight
//                    }
//                }
            }
        }

        return lines
    }

    private fun chordSpaceFragment(): LyricsFragment {
        val boldSpaceWidth = lengthMapper.get(LyricsTextType.CHORDS, ' ')
        return LyricsFragment(" ", type = LyricsTextType.CHORDS, width = boldSpaceWidth)
    }

    private fun areFragmentsBlank(fragments: List<LyricsFragment>): Boolean {
        return fragments.all { fragment -> fragment.text.isBlank() }
    }

    private fun filterFragments(fragments: List<LyricsFragment>, textType: LyricsTextType): List<LyricsFragment> {
        return fragments.filter { fragment -> fragment.type == textType }
    }

    private fun extractChordsAbove(lines: List<LyricsLine>): MutableList<LyricsLine> {
        val splitLines: MutableList<LyricsLine> = mutableListOf()
        lines.forEach { line ->
            val chords = LyricsLine(filterFragments(line.fragments, LyricsTextType.CHORDS))
            val texts = LyricsLine(filterFragments(line.fragments, LyricsTextType.REGULAR_TEXT))

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

}
