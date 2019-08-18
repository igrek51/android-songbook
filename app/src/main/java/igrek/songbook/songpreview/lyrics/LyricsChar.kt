package igrek.songbook.songpreview.lyrics

data class LyricsChar(
        var c: String,
        var width: Float,
        var type: LyricsTextType
) {

    override fun toString(): String {
        return c
    }
}
