package igrek.songbook.chords.lyrics.model

import com.google.common.base.Joiner

data class LyricsModel(
        val lines: List<LyricsLine> = listOf()
) {
    constructor(vararg lines: LyricsLine) : this(lines.toList())

    override fun toString(): String {
        return Joiner.on("\n").join(lines)
    }
}

data class LyricsLine(
        val fragments: List<LyricsFragment> = listOf()
) {
    constructor(vararg fragments: LyricsFragment) : this(fragments.toList())

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
        val txt = when (type) {
            LyricsTextType.REGULAR_TEXT -> text
            LyricsTextType.CHORDS -> "[$text]"
            LyricsTextType.LINEWRAPPER -> "\u21B5"
        }
        return "($txt,x=$x,width=$width)"
    }

    companion object {
        val lineWrapper = LyricsFragment(
                text = "\u21B5",
                type = LyricsTextType.LINEWRAPPER,
                width = 0f
        )
    }
}

enum class LyricsTextType {

    REGULAR_TEXT,

    CHORDS,

    LINEWRAPPER,

}
