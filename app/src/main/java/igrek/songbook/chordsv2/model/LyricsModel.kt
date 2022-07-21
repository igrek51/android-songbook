package igrek.songbook.chordsv2.model

import com.google.common.base.Joiner


data class LyricsModel(
    val lines: List<LyricsLine> = listOf(),
) {
    override fun toString(): String {
        return Joiner.on("\n").join(lines)
    }
}

data class LyricsLine(
    val fragments: List<LyricsFragment> = listOf(),
) {
    override fun toString(): String {
        return Joiner.on("").join(fragments)
    }

    val isBlank: Boolean = fragments.all { fragment -> fragment.text.isBlank() }
    val maxRightX: Float = fragments.maxOfOrNull { it.rightX } ?: 0f
}

data class LyricsFragment(
    var text: String,
    val type: LyricsTextType,
    var x: Float = 0f,
    var width: Float = 0f,
    var chordFragments: List<ChordFragment> = listOf(),
) {
    override fun toString(): String {
        val txt = when (type) {
            LyricsTextType.REGULAR_TEXT -> text
            LyricsTextType.CHORDS -> "[$text]"
            LyricsTextType.COMMENT -> "{$text}"
            LyricsTextType.LINEWRAPPER -> lineWrapperChar.toString()
        }
        return "($txt,x=$x,width=$width)"
    }

    val rightX: Float = x + width

    companion object {
        val lineWrapper = LyricsFragment(
            text = lineWrapperChar.toString(),
            type = LyricsTextType.LINEWRAPPER,
            width = 0f
        )

        fun text(text: String, x: Float = 0f, width: Float = 0f): LyricsFragment {
            return LyricsFragment(text, LyricsTextType.REGULAR_TEXT, x = x, width = width)
        }

        fun chords(text: String, x: Float = 0f, width: Float = 0f): LyricsFragment {
            return LyricsFragment(text, LyricsTextType.CHORDS, x = x, width = width)
        }
    }
}

// A chord composed of multiple single chords, eg. Cadd9/G
data class CompoundChord(
    var text: String,
    var chordFragments: List<ChordFragment> = listOf(),
)

data class ChordFragment(
    var text: String,
    val type: ChordFragmentType,
    var x: Float = 0f,
    var width: Float = 0f,
    val chord: Chord? = null,
) {
    override fun toString(): String {
        return text
    }
}


enum class LyricsTextType {
    REGULAR_TEXT,
    CHORDS,
    COMMENT,
    LINEWRAPPER,
}

enum class ChordFragmentType {
    CHORD,
    CHORD_SPLITTER,
}

const val lineWrapperChar = '\u21B5'
