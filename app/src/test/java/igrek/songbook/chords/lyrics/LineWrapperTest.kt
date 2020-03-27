package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsTextType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LineWrapperTest {

    @Test
    fun test_wrap_short_line() {
        val charLength = hashMapOf(' ' to 1f, 'a' to 2f, 'b' to 2f)
        val lineWrapper = LineWrapper(screenWEm = 1024f, normalCharLengths = charLength, boldCharLengths = charLength)
        val wrapped = lineWrapper.wrapLine(LyricsLine(listOf(
                LyricsFragment(text = "a b", type = LyricsTextType.REGULAR_TEXT, widthEm = 5f)
        )))
        assertThat(wrapped).isEqualTo(listOf(LyricsLine(listOf(
                LyricsFragment(text = "a b", type = LyricsTextType.REGULAR_TEXT, widthEm = 5f)
        ))))
    }

    @Test
    fun test_wrap_word_end() {
        val charLength = hashMapOf(' ' to 1f, 'a' to 1f, 'b' to 1f)
        val lineWrapper = LineWrapper(screenWEm = 4f, normalCharLengths = charLength, boldCharLengths = charLength)
        val wrapped = lineWrapper.wrapLine(LyricsLine(listOf(
                LyricsFragment(text = "aa bb aa", type = LyricsTextType.REGULAR_TEXT, widthEm = 8f)
        )))
        assertThat(wrapped).isEqualTo(listOf(
                LyricsLine(listOf(
                        LyricsFragment(text = "aa ", type = LyricsTextType.REGULAR_TEXT, widthEm = 3f),
                        LyricsFragment(text = "\u21B5", type = LyricsTextType.LINEWRAPPER)
                )),
                LyricsLine(listOf(
                        LyricsFragment(text = "bb ", type = LyricsTextType.REGULAR_TEXT, widthEm = 3f),
                        LyricsFragment(text = "\u21B5", type = LyricsTextType.LINEWRAPPER)
                )),
                LyricsLine(listOf(
                        LyricsFragment(text = "aa", type = LyricsTextType.REGULAR_TEXT, widthEm = 2f)
                ))
        ))
    }

    @Test
    fun test_very_long_word() {
        val charLength = hashMapOf(' ' to 1f, 'a' to 1f)
        val lineWrapper = LineWrapper(screenWEm = 3f, normalCharLengths = charLength, boldCharLengths = charLength)
        val wrapped = lineWrapper.wrapLine(LyricsLine(listOf(
                LyricsFragment(text = "aaaaaaa", type = LyricsTextType.REGULAR_TEXT, widthEm = 8f)
        )))
        assertThat(wrapped).isEqualTo(listOf(
                LyricsLine(listOf(
                        LyricsFragment(text = "aaa", type = LyricsTextType.REGULAR_TEXT, widthEm = 3f),
                        LyricsFragment(text = "\u21B5", type = LyricsTextType.LINEWRAPPER)
                )),
                LyricsLine(listOf(
                        LyricsFragment(text = "aaa", type = LyricsTextType.REGULAR_TEXT, widthEm = 3f),
                        LyricsFragment(text = "\u21B5", type = LyricsTextType.LINEWRAPPER)
                )),
                LyricsLine(listOf(
                        LyricsFragment(text = "a", type = LyricsTextType.REGULAR_TEXT, widthEm = 1f)
                ))
        ))
    }

    @Test
    fun test_mixed_chords_split() {
        val charLength = hashMapOf(' ' to 1f, 'a' to 1f, 'F' to 1f)
        val lineWrapper = LineWrapper(screenWEm = 3f, normalCharLengths = charLength, boldCharLengths = charLength)
        val wrapped = lineWrapper.wrapLine(LyricsLine(listOf(
                LyricsFragment(text = "a", type = LyricsTextType.REGULAR_TEXT, widthEm = 1f),
                LyricsFragment(text = "aaF", type = LyricsTextType.CHORDS, widthEm = 3f)
        )))
        assertThat(wrapped).isEqualTo(listOf(
                LyricsLine(listOf(
                        LyricsFragment(text = "a", type = LyricsTextType.REGULAR_TEXT, widthEm = 1f),
                        LyricsFragment(text = "\u21B5", type = LyricsTextType.LINEWRAPPER)
                )),
                LyricsLine(listOf(
                        LyricsFragment(text = "aaF", type = LyricsTextType.CHORDS, widthEm = 3f)
                ))
        ))
    }

}