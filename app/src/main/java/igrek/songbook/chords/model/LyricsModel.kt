package igrek.songbook.chords.model


data class LyricsModel(
    val lines: List<LyricsLine> = listOf(),
) {
    override fun toString(): String {
        return lines.joinToString("\n")
    }

    fun displayString(): String {
        return lines.joinToString (separator = "\n") { it.displayString() }
    }
}

data class LyricsLine(
    val fragments: List<LyricsFragment> = listOf(),
) {
    override fun toString(): String {
        return fragments.joinToString("")
    }

    fun displayString(): String {
        return fragments.joinToString (separator = "") { it.displayString() }
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
            LyricsTextType.CHORDS -> "[$text($chordFragments)]"
            LyricsTextType.COMMENT -> "{$text}"
            LyricsTextType.LINEWRAPPER -> lineWrapperChar.toString()
        }
        return "($txt,x=$x,width=$width)"
    }

    fun displayString(): String {
        return when (type) {
            LyricsTextType.REGULAR_TEXT -> text
            LyricsTextType.CHORDS -> "[$text]"
            LyricsTextType.COMMENT -> "{$text}"
            LyricsTextType.LINEWRAPPER -> lineWrapperChar.toString()
        }
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
            return LyricsFragment(text, LyricsTextType.CHORDS, x = x, width = width, chordFragments = listOf())
        }
    }
}

enum class LyricsTextType {
    REGULAR_TEXT,
    CHORDS,
    COMMENT,
    LINEWRAPPER,
}

data class ChordFragment(
    var text: String,
    val type: ChordFragmentType,
    var x: Float = 0f,
    var width: Float = 0f,
    val singleChord: Chord? = null,
    val compoundChord: CompoundChord? = null,
)

enum class ChordFragmentType {
    SINGLE_CHORD,
    COMPOUND_CHORD,
    CHORD_SPLITTER,
    UNKNOWN_CHORD,
}

const val lineWrapperChar = '\u21B5'
