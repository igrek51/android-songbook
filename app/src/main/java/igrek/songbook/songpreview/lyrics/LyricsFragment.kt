package igrek.songbook.songpreview.lyrics

class LyricsFragment(var x: Float, var text: String, var type: LyricsTextType?) {

    override fun toString(): String {
        return text
    }
}

