package igrek.songbook.songpreview.lyrics

data class LyricsFragment(
        var x: Float,
        var text: String,
        var type: LyricsTextType,
        var width: Float
) {

    override fun toString(): String {
        return text
    }
}
