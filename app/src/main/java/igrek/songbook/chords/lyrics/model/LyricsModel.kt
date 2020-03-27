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
        var text: String,
        val type: LyricsTextType,
        var x: Float = 0f,
        var width: Float = 0f
) {

    override fun toString(): String {
        val txt = when (type) {
            LyricsTextType.REGULAR_TEXT -> text
            LyricsTextType.CHORDS -> "[$text]"
            LyricsTextType.LINEWRAPPER -> lineWrapperChar.toString()
        }
        return "($txt,x=$x,width=$width)"
    }

    companion object {
        val lineWrapper = LyricsFragment(
                text = lineWrapperChar.toString(),
                type = LyricsTextType.LINEWRAPPER,
                width = 0f
        )

        fun Text(text: String, x: Float = 0f, width: Float = 0f): LyricsFragment {
            return LyricsFragment(text, LyricsTextType.REGULAR_TEXT, x = x, width = width)
        }

        fun Chord(text: String, x: Float = 0f, width: Float = 0f): LyricsFragment {
            return LyricsFragment(text, LyricsTextType.CHORDS, x = x, width = width)
        }
    }
}

enum class LyricsTextType {

    REGULAR_TEXT,

    CHORDS,

    LINEWRAPPER,

}

const val lineWrapperChar = '\u21B5'