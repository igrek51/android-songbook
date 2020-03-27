package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsTextType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LineWrapperTest {

    private val lengthMapper = TypefaceLengthMapper(
            ' ' to 1f,
            'a' to 1f,
            'b' to 1f,
            'o' to 1f,
            'c' to 2f,
            'd' to 2f,
            'F' to 1f,
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
                        LyricsFragment.lineWrapper.apply { x = 4f }
                ),
                LyricsLine(
                        LyricsFragment(text = "bb ", type = LyricsTextType.REGULAR_TEXT, width = 3f),
                        LyricsFragment.lineWrapper.apply { x = 4f }
                ),
                LyricsLine(
                        LyricsFragment(text = "aa", type = LyricsTextType.REGULAR_TEXT, width = 2f),
                )
        )
    }

    @Test
    fun test_very_long_word() {
        val lineWrapper = LineWrapper(screenWRelative = 3f, lengthMapper = lengthMapper)
        val wrapped = lineWrapper.wrapLine(LyricsLine(
                LyricsFragment(text = "aaaaaaa", type = LyricsTextType.REGULAR_TEXT, width = 8f),
        ))
        assertThat(wrapped).containsExactly(
                LyricsLine(
                        LyricsFragment(text = "aaa", type = LyricsTextType.REGULAR_TEXT, width = 3f),
                        LyricsFragment.lineWrapper.apply { x = 3f }
                ),
                LyricsLine(
                        LyricsFragment(text = "aaa", type = LyricsTextType.REGULAR_TEXT, width = 3f),
                        LyricsFragment.lineWrapper.apply { x = 3f }
                ),
                LyricsLine(
                        LyricsFragment(text = "a", type = LyricsTextType.REGULAR_TEXT, width = 1f),
                )
        )
    }

    @Test
    fun test_mixed_chords_split() {
        val lineWrapper = LineWrapper(screenWRelative = 3f, lengthMapper = lengthMapper)
        val wrapped = lineWrapper.wrapLine(LyricsLine(
                LyricsFragment(text = "a", type = LyricsTextType.REGULAR_TEXT, width = 1f),
                LyricsFragment(text = "aaF", type = LyricsTextType.CHORDS, width = 3f),
        ))
        assertThat(wrapped).containsExactly(
                LyricsLine(
                        LyricsFragment(text = "a", type = LyricsTextType.REGULAR_TEXT, width = 1f),
                        LyricsFragment.lineWrapper.apply { x = 3f }
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
                        LyricsFragment.lineWrapper.apply { x = 9f }
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

}