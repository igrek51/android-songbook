package igrek.songbook.chordsv2.arranger

import igrek.songbook.chordsv2.formatter.TypefaceLengthMapper
import igrek.songbook.chordsv2.model.LyricsFragment
import igrek.songbook.chordsv2.model.LyricsTextType


internal fun text(str: String, x: Float = 0f): LyricsFragment {
    return LyricsFragment.text(str, width = str.length.toFloat(), x = x)
}

internal fun chord(str: String, x: Float = 0f): LyricsFragment {
    return LyricsFragment.chords(str, width = str.length.toFloat(), x = x)
}

internal fun linewrapper(screenWRelative: Float): LyricsFragment {
    return LyricsFragment.lineWrapper.apply { x = screenWRelative - 1f; width = 1f }
}

internal val equalLengthMapper = object : TypefaceLengthMapper() {
    override fun charWidth(type: LyricsTextType, char: Char): Float {
        return 1f
    }
}
