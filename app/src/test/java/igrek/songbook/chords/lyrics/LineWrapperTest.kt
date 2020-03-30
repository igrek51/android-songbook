package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsTextType
import igrek.songbook.chords.lyrics.model.lineWrapperChar
import igrek.songbook.chords.lyrics.wrapper.LineWrapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LineWrapperTest {

    private val lengthMapper = TypefaceLengthMapper(
            lineWrapperChar to 1f,
            ' ' to 1f,
            'a' to 1f,
            'b' to 1f,
            'o' to 1f,
            'c' to 2f,
            'd' to 2f,
            'F' to 1f,
            'C' to 1f,
            'G' to 1f,
    )

    @Test
    fun test_wrap_short_line() {
        val lineWrapper = LineWrapper(screenWRelative = 1024f, lengthMapper = lengthMapper)
        val wrapped = lineWrapper.wrapLine(LyricsLine(
                LyricsFragment(text = "c d", type = LyricsTextType.REGULAR_TEXT, width = 5f),
        ))
        assertThat(wrapped).containsExactly(LyricsLine(
                LyricsFragment(text = "c d", type = LyricsTextType.REGULAR_TEXT, width = 5f),
        ))
    }

    @Test
    fun test_wrap_word_end() {
        val lineWrapper = LineWrapper(screenWRelative = 4f, lengthMapper = lengthMapper)
        val wrapped = lineWrapper.wrapLine(LyricsLine(
                LyricsFragment(text = "aa bb aa", type = LyricsTextType.REGULAR_TEXT, width = 8f),
        ))
        assertThat(wrapped).containsExactly(
                LyricsLine(
                        LyricsFragment(text = "aa ", type = LyricsTextType.REGULAR_TEXT, width = 3f),
                        LyricsFragment.lineWrapper.apply { x = 3f; width = 1f }
                ),
                LyricsLine(
                        LyricsFragment(text = "bb ", type = LyricsTextType.REGULAR_TEXT, width = 3f),
                        LyricsFragment.lineWrapper.apply { x = 3f; width = 1f }
                ),
                LyricsLine(
                        LyricsFragment(text = "aa", type = LyricsTextType.REGULAR_TEXT, width = 2f),
                )
        )
    }

    @Test
    fun test_very_long_word_left_intact() {
        val lineWrapper = LineWrapper(screenWRelative = 3f, lengthMapper = lengthMapper)
        val wrapped = lineWrapper.wrapLine(LyricsLine(
                text("aaaaaaa"),
        ))
        assertThat(wrapped).containsExactly(
                LyricsLine(
                        LyricsFragment.Text(text = "aaaaaaa", width = 7f),
                ),
        )
    }

    @Test
    fun test_mixed_chords_split() {
        val lineWrapper = LineWrapper(screenWRelative = 3f, lengthMapper = lengthMapper)
        val wrapped = lineWrapper.wrapLine(LyricsLine(
                text("a", x = 0f),
                chord("aaF", x = 1f),
        ))
        assertThat(wrapped).containsExactly(
                LyricsLine(
                        LyricsFragment(text = "a", type = LyricsTextType.REGULAR_TEXT, width = 1f),
                        LyricsFragment.lineWrapper.apply { x = 2f; width = 1f }
                ),
                LyricsLine(
                        LyricsFragment(text = "aaF", type = LyricsTextType.CHORDS, width = 3f),
                )
        )
    }

    @Test
    fun test_many_words() {
        val lineWrapper = LineWrapper(screenWRelative = 9f, lengthMapper = lengthMapper)
        val wrapped = lineWrapper.wrapLine(LyricsLine(
                LyricsFragment(text = "baba ab bab", type = LyricsTextType.REGULAR_TEXT, width = 11f),
        ))
        assertThat(wrapped).containsExactly(
                LyricsLine(
                        LyricsFragment(text = "baba ab ", type = LyricsTextType.REGULAR_TEXT, width = 8f),
                        LyricsFragment.lineWrapper.apply { x = 8f; width = 1f }
                ),
                LyricsLine(
                        LyricsFragment(text = "bab", type = LyricsTextType.REGULAR_TEXT, width = 3f),
                )
        )
    }

    @Test
    fun test_many_words_unwrapped() {
        val lineWrapper = LineWrapper(screenWRelative = 100f, lengthMapper = lengthMapper)
        val wrapped = lineWrapper.wrapLine(LyricsLine(
                LyricsFragment(text = "baba ab", type = LyricsTextType.REGULAR_TEXT, width = 7f, x = 0f),
                LyricsFragment(text = "a F", type = LyricsTextType.CHORDS, width = 3f, x = 7f),
                LyricsFragment(text = "baobab", type = LyricsTextType.REGULAR_TEXT, width = 6f, x = 10f),
        ))
        assertThat(wrapped).containsExactly(
                LyricsLine(
                        LyricsFragment(text = "baba ab", type = LyricsTextType.REGULAR_TEXT, width = 7f, x = 0f),
                        LyricsFragment(text = "a F", type = LyricsTextType.CHORDS, width = 3f, x = 7f),
                        LyricsFragment(text = "baobab", type = LyricsTextType.REGULAR_TEXT, width = 6f, x = 10f),
                )
        )
    }

    @Test
    fun test_wrapping_mixed_types() {
        val lineWrapper = LineWrapper(screenWRelative = 20f, lengthMapper = lengthMapper)
        val wrapped = lineWrapper.wrapLine(LyricsLine(
                chord("G", x = 0f),
                text("abo obobabao", x = 1f),
                chord("F", x = 13f),
                text("ba ba ba", x = 14f),
        ))
        assertThat(wrapped).containsExactly(
                LyricsLine(
                        LyricsFragment.Chord("G", x = 0f, width = 1f),
                        LyricsFragment.Text("abo obobabao", x = 1f, width = 12f),
                        LyricsFragment.Chord("F", x = 13f, width = 1f),
                        LyricsFragment.Text("ba ba ", x = 14f, width = 6f),
                        LyricsFragment.lineWrapper.apply { x = 19f; width = 1f }
                ),
                LyricsLine(
                        LyricsFragment.Text("ba", x = 0f, width = 2f),
                )
        )
    }

}