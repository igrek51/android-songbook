package igrek.songbook.songpreview.lyrics

class LyricsChar(var c: String, var width: Float, var type: LyricsTextType) {

    override fun toString(): String {
        return c
    }
}
