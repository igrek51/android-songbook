package igrek.songbook.chords.arranger

import igrek.songbook.chords.render.TypefaceLengthMapper
import igrek.songbook.chords.model.LyricsFragment
import igrek.songbook.chords.model.LyricsLine
import igrek.songbook.chords.model.LyricsModel
import igrek.songbook.chords.model.LyricsTextType
import igrek.songbook.chords.model.lineWrapperChar
import igrek.songbook.settings.theme.DisplayStyle
import org.assertj.core.api.Assertions
import org.junit.Test

class LyricsArrangerTest {

    private val lengthMapper = TypefaceLengthMapper(
        lineWrapperChar to 1f,
        ' ' to 1f,
        'a' to 1f,
        'b' to 1f,
        'c' to 1f,
        'e' to 1f,
        'o' to 1f,
        'm' to 1f,
        'r' to 1f,
        's' to 1f,
        'r' to 1f,
        'i' to 1f,
        'n' to 1f,
        'k' to 1f,
        'w' to 1f,
        'F' to 1f,
        'C' to 1f,
        '7' to 1f,
        'G' to 1f,
        'D' to 1f,
    )

    @Test
    fun test_chords_inline_untouched() {
        val wrapper = LyricsArranger(
            displayStyle = DisplayStyle.ChordsInline,
            screenWRelative = 100f,
            lengthMapper = lengthMapper
        )
        val wrapped = wrapper.arrangeModel(
            LyricsModel(listOf(
                LyricsLine(listOf(
                    LyricsFragment.text("baba ab ", width = 8f, x = 0f),
                    LyricsFragment.chords("a F", width = 3f, x = 8f),
                    LyricsFragment.text(" baobab", width = 7f, x = 11f),
                ))
            ))
        )
        Assertions.assertThat(wrapped.lines).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.text("baba ab", width = 8f, x = 0f),
                LyricsFragment.chords("a F", width = 3f, x = 8f),
                LyricsFragment.text(" baobab", width = 7f, x = 11f),
            ))
        )
    }

    @Test
    fun test_chords_only() {
        val wrapper = LyricsArranger(
            displayStyle = DisplayStyle.ChordsOnly,
            screenWRelative = 100f,
            lengthMapper = lengthMapper
        )
        val wrapped = wrapper.arrangeModel(
            LyricsModel(listOf(
                LyricsLine(listOf(
                    LyricsFragment.text("bb", width = 2f),
                    LyricsFragment.chords("a", width = 1f),
                    LyricsFragment.text("bb", width = 2f),
                ))
            ))
        )
        Assertions.assertThat(wrapped.lines).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.chords("a", x = 0f, width = 1f),
            ))
        )
    }

    @Test
    fun test_lyrics_only() {
        val wrapper = LyricsArranger(
            displayStyle = DisplayStyle.LyricsOnly,
            screenWRelative = 100f,
            lengthMapper = lengthMapper
        )
        val wrapped = wrapper.arrangeModel(
            LyricsModel(listOf(
                LyricsLine(listOf(
                    LyricsFragment.text("bb", width = 2f),
                    LyricsFragment.chords("a", width = 1f),
                    LyricsFragment.text("bb", width = 2f),
                )),
            )),
        )
        Assertions.assertThat(wrapped.lines).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.text("bb", x = 0f, width = 2f),
                LyricsFragment.text("bb", x = 2f, width = 2f),
            )),
        )
    }

    @Test
    fun test_chords_aligned_right() {
        val wrapper = LyricsArranger(
            displayStyle = DisplayStyle.ChordsAlignedRight,
            screenWRelative = 100f,
            lengthMapper = lengthMapper
        )
        val wrapped = wrapper.arrangeModel(
            LyricsModel(listOf(
                LyricsLine(listOf(
                    text("bb"),
                    chord("a F"),
                    text(" "),
                    chord("C"),
                    text("bb"),
                ))
            )),
        )
        Assertions.assertThat(wrapped.lines).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.text("bb", x = 0f, width = 2f),
                LyricsFragment.text("bb", x = 3f, width = 3f),
                LyricsFragment.chords("a F", x = 94f, width = 3f),
                LyricsFragment.chords(" C", x = 97f, width = 2f),
            )),
        )
    }

    @Test
    fun test_chords_above() {
        val wrapper = LyricsArranger(
            displayStyle = DisplayStyle.ChordsAbove,
            screenWRelative = 100f,
            lengthMapper = lengthMapper
        )
        val wrapped = wrapper.arrangeModel(
            LyricsModel(listOf(
                LyricsLine(listOf(
                    text("here"),
                    chord("a F"),
                    text("goes"),
                    chord("C"),
                    text("accent"),
                )),
            )),
        )
        Assertions.assertThat(wrapped.lines).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.chords("a F", x = 4f, width = 3f),
                LyricsFragment.chords("C", x = 8f, width = 1f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("here", x = 0f, width = 4f),
                LyricsFragment.text("goes", x = 4f, width = 4f),
                LyricsFragment.text("accent", x = 8f, width = 6f),
            )),
        )
    }

    @Test
    fun test_wrapping_chords_joining_groups() {
        val screenWRelative = 32f
        val wrapper = LyricsArranger(DisplayStyle.ChordsInline, screenWRelative, lengthMapper)
        val wrapped = wrapper.arrangeModel(
            LyricsModel(listOf(
                LyricsLine(listOf(
                    chord("G"),
                    text("mrs robinson"),
                    chord("e"),
                    text("know"),
                    chord("a7"),
                    chord("D"),
                    text("wo wo wo"),
                )),
            )),
        )
        Assertions.assertThat(wrapped.lines).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.chords("G", x = 0f, width = 1f),
                LyricsFragment.text(" mrs robinson", x = 1f, width = 13f),
                LyricsFragment.chords("e", x = 14f, width = 1f),
                LyricsFragment.text("know", x = 15f, width = 5f),
                LyricsFragment.chords("a7 D", x = 20f, width = 4f),
                LyricsFragment.text(" wo wo", x = 24f, width = 7f),
                linewrapper(screenWRelative),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("wo", x = 0f, width = 2f),
            )),
        )
    }

    @Test
    fun test_text_not_wrapped_when_chords_above() {
        val wrapper = LyricsArranger(
            displayStyle = DisplayStyle.ChordsAbove,
            screenWRelative = 17f,
            lengthMapper = lengthMapper
        )
        val wrapped = wrapper.arrangeModel(
            LyricsModel(listOf(
                LyricsLine(listOf(
                    chord("G e a7 D"),
                    text("wo wo wo"),
                    chord("G e a7 D"),
                    text("wo wo wo"),
                )),
            ))
        )
        /*
        G e a7 D G e a7 D
        wo wo wo wo wo wo
         */
        Assertions.assertThat(wrapped.lines).isEqualTo(listOf(
            LyricsLine(listOf(
                LyricsFragment.chords("G e a7 D", x = 0f, width = 8f),
                LyricsFragment.chords("G e a7 D", x = 9f, width = 8f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("wo wo wo", x = 0f, width = 8f),
                LyricsFragment.text("wo wo wo", x = 9f, width = 8f),
            )),
        ))
    }

    @Test
    fun test_wrapped_when_chords_above() {
        val wrapper = LyricsArranger(
            displayStyle = DisplayStyle.ChordsAbove,
            screenWRelative = 8f,
            lengthMapper = lengthMapper
        )
        val wrapped = wrapper.arrangeModel(
            LyricsModel(listOf(
                LyricsLine(listOf(
                    chord("G e a7 D"),
                    text("wo wo wo"),
                    chord("G e a7 D"),
                    text("wo wo wo"),
                )),
            )),
        )
        Assertions.assertThat(wrapped.lines).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.chords("G e a7 D", x = 0f, width = 8f),
                linewrapper(8f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("wo wo wo", x = 0f, width = 8f),
                linewrapper(8f),
            )),
            LyricsLine(listOf(
                LyricsFragment.chords("G e a7 D", x = 0f, width = 8f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("wo wo wo", x = 0f, width = 8f),
            )),
        )
    }

    @Test
    fun test_inline_chords_padding() {
        val wrapper = LyricsArranger(
            displayStyle = DisplayStyle.ChordsInline,
            screenWRelative = 100f,
            lengthMapper = lengthMapper
        )
        val wrapped = wrapper.arrangeModel(
            LyricsModel(listOf(
                LyricsLine(listOf(
                    chord("A"),
                    text("before. "),
                    chord("B"),
                    text(" after"),
                    chord("H"),
                )),
                LyricsLine(listOf(
                    text("middle"),
                    chord("A"),
                    text("word"),
                )),
            )),
        )
        Assertions.assertThat(wrapped.lines).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.chords("A", width = 1f, x = 0f),
                LyricsFragment.text(" before.", width = 9f, x = 1f),
                LyricsFragment.chords("B", width = 1f, x = 10f),
                LyricsFragment.text(" after", width = 7f, x = 11f),
                LyricsFragment.chords("H", width = 1f, x = 18f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("middle", width = 6f, x = 0f),
                LyricsFragment.chords("A", width = 1f, x = 6f),
                LyricsFragment.text("word", width = 4f, x = 7f),
            )),
        )
    }

    @Test
    fun test_align_to_left_single_chords_section() {
        val wrapper = LyricsArranger(
            displayStyle = DisplayStyle.ChordsAbove,
            screenWRelative = 100f,
            lengthMapper = lengthMapper
        )
        val wrapped = wrapper.arrangeModel(
            LyricsModel(listOf(
                LyricsLine(listOf(
                    text("wo lo lo"),
                    chord("G e"),
                )),
                LyricsLine(listOf(
                    text("wo lo lo"),
                    chord("G"),
                    chord("e"),
                )),
            )),
        )
        /*
        Should be:
        G e
        wo lo lo
                G e
        wo lo lo
         */
        Assertions.assertThat(wrapped.lines).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.chords("G e", x = 0f, width = 3f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("wo lo lo", x = 0f, width = 8f),
            )),
            LyricsLine(listOf(
                LyricsFragment.chords("G", x = 8f, width = 1f),
                LyricsFragment.chords("e", x = 10f, width = 1f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("wo lo lo", x = 0f, width = 8f),
            )),
        )
    }

    @Test
    fun test_align_move_overlapped_chords() {
        val wrapper = LyricsArranger(
            displayStyle = DisplayStyle.ChordsAbove,
            screenWRelative = 100f,
            lengthMapper = lengthMapper
        )
        val wrapped = wrapper.arrangeModel(
            LyricsModel(listOf(
                LyricsLine(listOf(
                    chord("C"),
                    text("c"),
                    chord("F"),
                    text("f"),
                )),
                LyricsLine(listOf(
                    text("wolo"),
                    chord("G"),
                    chord("e"),
                    text("end"),
                )),
            )),
        )
        /*
        Should be:
        C F
        c f
            G e
        wolo  end
         */
        Assertions.assertThat(wrapped.lines).isEqualTo(listOf(
            LyricsLine(listOf(
                LyricsFragment.chords("C", x = 0f, width = 1f),
                LyricsFragment.chords("F", x = 2f, width = 1f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("c", x = 0f, width = 1f),
                LyricsFragment.text("f", x = 2f, width = 1f),
            )),
            LyricsLine(listOf(
                LyricsFragment.chords("G", x = 4f, width = 1f),
                LyricsFragment.chords("e", x = 6f, width = 1f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("wolo", x = 0f, width = 4f),
                LyricsFragment.text("end", x = 6f, width = 3f),
            )),
        ))
    }

    @Test
    fun test_wrapping_off_when_horizontal_scrolling() {
        val wrapper =
            LyricsArranger(DisplayStyle.ChordsInline, 16f, lengthMapper, horizontalScroll = true)
        val wrapped = wrapper.arrangeModel(
            LyricsModel(listOf(
                LyricsLine(listOf(
                    chord("G"),
                    text("mrs robinson"),
                    chord("e"),
                    text("know"),
                    chord("a7"),
                    chord("D"),
                    text("wo wo wo"),
                )),
            )),
        )
        Assertions.assertThat(wrapped.lines).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.chords("G", x = 0f, width = 1f),
                LyricsFragment.text(" mrs robinson", x = 1f, width = 13f),
                LyricsFragment.chords("e", x = 14f, width = 1f),
                LyricsFragment.text("know", x = 15f, width = 5f),
                LyricsFragment.chords("a7", x = 20f, width = 2f),
                LyricsFragment.chords(" D", x = 22f, width = 2f),
                LyricsFragment.text(" wo wo wo", x = 24f, width = 9f),
            )),
        )
    }


    @Test
    fun test_chords_above_with_comment() {
        val wrapper = LyricsArranger(
            displayStyle = DisplayStyle.ChordsAbove,
            screenWRelative = 100f,
            lengthMapper = lengthMapper,
        )
        val wrapped = wrapper.arrangeModel(
            LyricsModel(listOf(
                LyricsLine(listOf(
                    text("end of verse"),
                    chord("a F"),
                    text(" "),
                    LyricsFragment("// x2", LyricsTextType.COMMENT, x = 0f, width = 5f),
                )),
            )),
        )
        Assertions.assertThat(wrapped.lines).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.chords("a F", x = 0f, width = 3f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("end of verse", x = 0f, width = 12f),
                LyricsFragment.text("", x = 12f, width = 1f),
                LyricsFragment("// x2", LyricsTextType.COMMENT, x = 13f, width = 5f),
            )),
        )
    }

}