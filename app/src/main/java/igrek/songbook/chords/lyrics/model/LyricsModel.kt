package igrek.songbook.chords.lyrics.model

import com.google.common.base.Joiner

data class LyricsModel(
        val lines: List<LyricsLine> = listOf()
) {

    override fun toString(): String {
        return Joiner.on("\n").join(lines)
    }
}

data class LyricsLine(
        val fragments: List<LyricsFragment> = listOf()
) {

    override fun toString(): String {
        return Joiner.on("").join(fragments)
    }

    fun isBlank(): Boolean {
        return fragments.all { fragment -> fragment.text.isBlank() }
    }
}

data class LyricsFragment(
        val text: String,
        val type: LyricsTextType,
        var x: Float = 0f,
        var width: Float = 0f
) {

    override fun toString(): String {
        return when (type) {
            LyricsTextType.REGULAR_TEXT -> text
            LyricsTextType.CHORDS -> "[$text]"
            LyricsTextType.LINEWRAPPER -> "\u21B5"
        }
    }
}

enum class LyricsTextType {

    REGULAR_TEXT,

    CHORDS,

    LINEWRAPPER,

}
