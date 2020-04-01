package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.lyrics.model.lineWrapperChar
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
        val wrapper = LyricsArranger(displayStyle = DisplayStyle.ChordsInline, screenWRelative = 100f, lengthMapper = lengthMapper)
        val wrapped = wrapper.arrangeModel(LyricsModel(
                LyricsLine(
                        LyricsFragment.Text("baba ab ", width = 8f, x = 0f),
                        LyricsFragment.Chord("a F", width = 3f, x = 8f),
                        LyricsFragment.Text(" baobab", width = 7f, x = 11f),
                )
        ))
        Assertions.assertThat(wrapped.lines).containsExactly(
                LyricsLine(
                        LyricsFragment.Text("baba ab", width = 8f, x = 0f),
                        LyricsFragment.Chord("a F", width = 3f, x = 8f),
                        LyricsFragment.Text(" baobab", width = 7f, x = 11f),
                )
        )
    }

    @Test
    fun test_chords_only() {
        val wrapper = LyricsArranger(displayStyle = DisplayStyle.ChordsOnly, screenWRelative = 100f, lengthMapper = lengthMapper)
        val wrapped = wrapper.arrangeModel(LyricsModel(
                LyricsLine(
                        LyricsFragment.Text("bb", width = 2f),
                        LyricsFragment.Chord("a", width = 1f),
                        LyricsFragment.Text("bb", width = 2f),
                )
        ))
        Assertions.assertThat(wrapped.lines).containsExactly(
                LyricsLine(
                        LyricsFragment.Chord("a", x = 0f, width = 1f),
                )
        )
    }

    @Test
    fun test_lyrics_only() {
        val wrapper = LyricsArranger(displayStyle = DisplayStyle.LyricsOnly, screenWRelative = 100f, lengthMapper = lengthMapper)
        val wrapped = wrapper.arrangeModel(LyricsModel(
                LyricsLine(
                        LyricsFragment.Text("bb", width = 2f),
                        LyricsFragment.Chord("a", width = 1f),
                        LyricsFragment.Text("bb", width = 2f),
                )
        ))
        Assertions.assertThat(wrapped.lines).containsExactly(
                LyricsLine(
                        LyricsFragment.Text("bb", x = 0f, width = 2f),
                        LyricsFragment.Text("bb", x = 2f, width = 2f),
                )
        )
    }

    @Test
    fun test_chords_aligned_right() {
        val wrapper = LyricsArranger(displayStyle = DisplayStyle.ChordsAlignedRight, screenWRelative = 100f, lengthMapper = lengthMapper)
        val wrapped = wrapper.arrangeModel(LyricsModel(
                LyricsLine(
                        text("bb"),
                        chord("a F"),
                        text(" "),
                        chord("C"),
                        text("bb"),
                )
        ))
        Assertions.assertThat(wrapped.lines).containsExactly(
                LyricsLine(
                        LyricsFragment.Text("bb", x = 0f, width = 2f),
                        LyricsFragment.Text("bb", x = 3f, width = 3f),
                        LyricsFragment.Chord("a F", x = 94f, width = 3f),
                        LyricsFragment.Chord(" C", x = 97f, width = 2f),
                )
        )
    }

    @Test
    fun test_chords_above() {
        val wrapper = LyricsArranger(displayStyle = DisplayStyle.ChordsAbove, screenWRelative = 100f, lengthMapper = lengthMapper)
        val wrapped = wrapper.arrangeModel(LyricsModel(
                LyricsLine(
                        text("here"),
                        chord("a F"),
                        text("goes"),
                        chord("C"),
                        text("accent"),
                ),
        ))
        Assertions.assertThat(wrapped.lines).containsExactly(
                LyricsLine(
                        LyricsFragment.Chord("a F", x = 4f, width = 3f),
                        LyricsFragment.Chord("C", x = 8f, width = 1f),
                ),
                LyricsLine(
                        LyricsFragment.Text("here", x = 0f, width = 4f),
                        LyricsFragment.Text("goes", x = 4f, width = 4f),
                        LyricsFragment.Text("accent", x = 8f, width = 6f),
                ),
        )
    }

    @Test
    fun test_wrapping_chords_joining_groups() {
        val screenWRelative = 32f
        val wrapper = LyricsArranger(DisplayStyle.ChordsInline, screenWRelative, lengthMapper)
        val wrapped = wrapper.arrangeModel(LyricsModel(
                LyricsLine(
                        chord("G"),
                        text("mrs robinson"),
                        chord("e"),
                        text("know"),
                        chord("a7"),
                        chord("D"),
                        text("wo wo wo"),
                ),
        ))
        Assertions.assertThat(wrapped.lines).containsExactly(
                LyricsLine(
                        LyricsFragment.Chord("G", x = 0f, width = 1f),
                        LyricsFragment.Text(" mrs robinson", x = 1f, width = 13f),
                        LyricsFragment.Chord("e", x = 14f, width = 1f),
                        LyricsFragment.Text("know", x = 15f, width = 5f),
                        LyricsFragment.Chord("a7 D", x = 20f, width = 4f),
                        LyricsFragment.Text(" wo wo", x = 24f, width = 7f),
                        linewrapper(screenWRelative),
                ),
                LyricsLine(
                        LyricsFragment.Text("wo", x = 0f, width = 2f),
                )
        )
    }

    @Test
    fun test_text_not_wrapped_when_chords_above() {
        val wrapper = LyricsArranger(displayStyle = DisplayStyle.ChordsAbove, screenWRelative = 16f, lengthMapper = lengthMapper)
        val wrapped = wrapper.arrangeModel(LyricsModel(
                LyricsLine(
                        chord("G e a7 D"),
                        text("wo wo wo"),
                        chord("G e a7 D"),
                        text("wo wo wo"),
                ),
        ))
        Assertions.assertThat(wrapped.lines).containsExactly(
                LyricsLine(
                        LyricsFragment.Chord("G e a7 D", x = 0f, width = 8f),
                        LyricsFragment.Chord("G e a7 D", x = 8f, width = 8f),
                ),
                LyricsLine(
                        LyricsFragment.Text("wo wo wo", x = 0f, width = 8f),
                        LyricsFragment.Text("wo wo wo", x = 8f, width = 8f),
                ),
        )
    }

    @Test
    fun test_wrapped_when_chords_above() {
        val wrapper = LyricsArranger(displayStyle = DisplayStyle.ChordsAbove, screenWRelative = 8f, lengthMapper = lengthMapper)
        val wrapped = wrapper.arrangeModel(LyricsModel(
                LyricsLine(
                        chord("G e a7 D"),
                        text("wo wo wo"),
                        chord("G e a7 D"),
                        text("wo wo wo"),
                ),
        ))
        Assertions.assertThat(wrapped.lines).containsExactly(
                LyricsLine(
                        LyricsFragment.Chord("G e a7 D", x = 0f, width = 8f),
                        linewrapper(8f),
                ),
                LyricsLine(
                        LyricsFragment.Text("wo wo wo", x = 0f, width = 8f),
                        linewrapper(8f),
                ),
                LyricsLine(
                        LyricsFragment.Chord("G e a7 D", x = 0f, width = 8f),
                ),
                LyricsLine(
                        LyricsFragment.Text("wo wo wo", x = 0f, width = 8f),
                ),
        )
    }

    @Test
    fun test_inline_chords_padding() {
        val wrapper = LyricsArranger(displayStyle = DisplayStyle.ChordsInline, screenWRelative = 100f, lengthMapper = lengthMapper)
        val wrapped = wrapper.arrangeModel(LyricsModel(
                LyricsLine(
                        chord("A"),
                        text("before. "),
                        chord("B"),
                        text(" after"),
                        chord("H"),
                ),
                LyricsLine(
                        text("middle"),
                        chord("A"),
                        text("word"),
                ),
        ))
        Assertions.assertThat(wrapped.lines).containsExactly(
                LyricsLine(
                        LyricsFragment.Chord("A", width = 1f, x = 0f),
                        LyricsFragment.Text(" before.", width = 9f, x = 1f),
                        LyricsFragment.Chord("B", width = 1f, x = 10f),
                        LyricsFragment.Text(" after", width = 7f, x = 11f),
                        LyricsFragment.Chord("H", width = 1f, x = 18f),
                ),
                LyricsLine(
                        LyricsFragment.Text("middle", width = 6f, x = 0f),
                        LyricsFragment.Chord("A", width = 1f, x = 6f),
                        LyricsFragment.Text("word", width = 4f, x = 7f),
                ),
        )
    }

}