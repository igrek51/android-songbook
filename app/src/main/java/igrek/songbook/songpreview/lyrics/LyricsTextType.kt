package igrek.songbook.songpreview.lyrics

enum class LyricsTextType constructor(val isDisplayable: Boolean) {

    REGULAR_TEXT(true),

    CHORDS(true),

    BRACKET(false),

    LINEWRAPPER(true)
}
