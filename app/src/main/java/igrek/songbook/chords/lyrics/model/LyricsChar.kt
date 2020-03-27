package igrek.songbook.chords.lyrics.model

data class LyricsChar(
        val c: Char,
        val type: LyricsTextType,
        var x: Float = 0f,
        var width: Float = 0f,
) {

    override fun toString(): String {
        return when (type) {
            LyricsTextType.REGULAR_TEXT -> "$c"
            LyricsTextType.CHORDS -> "[$c]"
            LyricsTextType.LINEWRAPPER -> lineWrapperChar.toString()
        }
    }

}