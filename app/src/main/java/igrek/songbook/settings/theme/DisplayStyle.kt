package igrek.songbook.settings.theme

import igrek.songbook.R

enum class DisplayStyle(val id: Long, val nameResId: Int) {

    ChordsInline(1, R.string.display_style_chords_inline),

    ChordsAbove(2, R.string.display_style_chords_above),

    ChordsAlignedRight(3, R.string.display_style_chords_aligned_right),

    ChordsOnly(4, R.string.display_style_chords_only),

    LyricsOnly(5, R.string.display_style_lyrics_only),

    ;

    companion object {
        val default = ChordsAbove

        fun parseById(id: Long): DisplayStyle? {
            return values().firstOrNull { v -> v.id == id }
        }

        fun mustParseById(id: Long): DisplayStyle {
            return parseById(id) ?: default
        }
    }
}
