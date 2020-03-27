package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.settings.theme.DisplayStyle
import org.assertj.core.api.Assertions
import org.junit.Test

class LyricsArrangerTest {

    private val lengthMapper = TypefaceLengthMapper(
            ' ' to 1f,
            'a' to 1f,
            'b' to 1f,
            'c' to 1f,
            'o' to 1f,
            'm' to 2f,
            'F' to 1f,
            'C' to 1f,
    )

    private fun text(str: String): LyricsFragment {
        return LyricsFragment.Text(str, width = str.length.toFloat())
    }

    private fun chord(str: String): LyricsFragment {
        return LyricsFragment.Chord(str, width = str.length.toFloat())
    }

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
    fun test_adding_inline_padding() {
        val wrapper = LyricsArranger(displayStyle = DisplayStyle.ChordsInline, screenWRelative = 100f, lengthMapper = lengthMapper)
        val wrapped = wrapper.arrangeModel(LyricsModel(
                LyricsLine(
                        LyricsFragment.Text("bb", width = 2f),
                        LyricsFragment.Chord("a", width = 1f),
                        LyricsFragment.Text("bb", width = 2f),
                )
        ))
        Assertions.assertThat(wrapped.lines).containsExactly(
                LyricsLine(
                        LyricsFragment.Text("bb", x = 0f, width = 3f),
                        LyricsFragment.Chord("a", x = 3f, width = 1f),
                        LyricsFragment.Text(" bb", x = 4f, width = 3f),
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
                )
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

}