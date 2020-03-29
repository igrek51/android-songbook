package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsTextType

internal fun text(str: String, x: Float = 0f): LyricsFragment {
    return LyricsFragment.Text(str, width = str.length.toFloat(), x = x)
}

internal fun chord(str: String, x: Float = 0f): LyricsFragment {
    return LyricsFragment.Chord(str, width = str.length.toFloat(), x = x)
}

internal fun linewrapper(screenWRelative: Float): LyricsFragment {
    return LyricsFragment.lineWrapper.apply { x = screenWRelative - 1f; width = 1f }
}

internal val equalLengthMapper = object : TypefaceLengthMapper() {
    override fun charWidth(type: LyricsTextType, char: Char): Float {
        return 1f
    }
}
