package igrek.songbook.chords.arranger

import igrek.songbook.chords.arranger.wordwrap.DoubleLineWrapper
import igrek.songbook.chords.model.LyricsFragment
import igrek.songbook.chords.model.LyricsLine
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DoubleLineWrapperTest {

    @Test
    fun test_unwrapped_short() {
        val wrapper = DoubleLineWrapper(screenWRelative = 10f, lengthMapper = equalLengthMapper)
        val wrapped = wrapper.wrapDoubleLine(chords = LyricsLine(listOf(
            chord("A", x = 0f),
        )), texts = LyricsLine(listOf(
            text("jolka", x = 0f),
        )))
        assertThat(wrapped).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.chords("A", x = 0f, width = 1f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("jolka", x = 0f, width = 5f),
            )),
        )
    }

    @Test
    fun test_wrapped_join_adjacent() {
        val screenWRelative = 10f
        val wrapper = DoubleLineWrapper(screenWRelative, equalLengthMapper)
        val wrapped = wrapper.wrapDoubleLine(chords = LyricsLine(listOf(
            chord("A", x = 0f),
            chord("F", x = 2f),
            chord("C", x = 7f),
        )), texts = LyricsLine(listOf(
            text("jolka", x = 0f),
            text("jolka", x = 5f),
            text("a", x = 10f),
        )))
        assertThat(wrapped).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.chords("A", x = 0f, width = 1f),
                LyricsFragment.chords("F", x = 2f, width = 1f),
                LyricsFragment.chords("C", x = 7f, width = 1f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("jolkajolka", x = 0f, width = 10f),
                linewrapper(screenWRelative),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("a", x = 0f, width = 1f),
            )),
        )
    }

    @Test
    fun test_wrapped_long_chords() {
        val wrapper = DoubleLineWrapper(screenWRelative = 7f, lengthMapper = equalLengthMapper)
        val wrapped = wrapper.wrapDoubleLine(chords = LyricsLine(listOf(
            chord("C D E F G A H C", x = 0f),
        )), texts = LyricsLine(listOf(
            text("jolka", x = 0f),
        )))
        assertThat(wrapped).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.chords("C D E ", x = 0f, width = 6f),
                linewrapper(7f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("jolka", x = 0f, width = 5f),
            )),
            LyricsLine(listOf(
                LyricsFragment.chords("F G A ", x = 0f, width = 6f),
                linewrapper(7f),
            )),
            LyricsLine(listOf(
                LyricsFragment.chords("H C", x = 0f, width = 3f),
            )),
        )
    }

    @Test
    fun test_next_line_moved_to_align() {
        val wrapper = DoubleLineWrapper(screenWRelative = 10f, lengthMapper = equalLengthMapper)
        val wrapped = wrapper.wrapDoubleLine(chords = LyricsLine(listOf(
            chord("C", x = 12f),
        )), texts = LyricsLine(listOf(
            text("jolkajolk ", x = 0f),
            text("jolka", x = 10f),
        )))
        assertThat(wrapped).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.text("jolkajolk ", x = 0f, width = 10f),
                linewrapper(10f),
            )),
            LyricsLine(listOf(
                LyricsFragment.chords("C", x = 2f, width = 1f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("jolka", x = 0f, width = 5f),
            )),
        )
    }

    @Test
    fun test_text_only_wrapped() {
        val wrapper = DoubleLineWrapper(screenWRelative = 18f, lengthMapper = equalLengthMapper)
        val wrapped = wrapper.wrapDoubleLine(chords = LyricsLine(listOf(
            chord("F#m", x = 15f),
        )), texts = LyricsLine(listOf(
            text("turning tables ", x = 0f),
            text("instead", x = 15f),
        )))
        assertThat(wrapped).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.chords("F#m", x = 15f, width = 3f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("turning tables ", x = 0f, width = 15f),
                linewrapper(18f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("instead", x = 0f, width = 7f),
            )),
        )
    }

    @Test
    fun test_chords_longer_wrap() {
        val wrapper = DoubleLineWrapper(screenWRelative = 26f, lengthMapper = equalLengthMapper)
        val wrapped = wrapper.wrapDoubleLine(chords = LyricsLine(listOf(
            chord("A", x = 0f),
            chord("F#m", x = 25f),
        )), texts = LyricsLine(listOf(
            text("dont think sorrys easily ", x = 0f),
            text("s", x = 25f),
        )))
        assertThat(wrapped).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.chords("A", x = 0f, width = 1f),
                linewrapper(26f),
            )),
            LyricsLine(listOf(
                LyricsFragment.text("dont think sorrys easily s", x = 0f, width = 26f),
            )),
            LyricsLine(listOf(
                LyricsFragment.chords("F#m", x = 0f, width = 3f),
            )),
        )
    }

    @Test
    fun test_chord_very_late() {
        val screenWRelative = 5f
        val wrapper = DoubleLineWrapper(screenWRelative, equalLengthMapper)
        val wrapped = wrapper.wrapDoubleLine(chords = LyricsLine(listOf(
            chord("A", x = 16f),
        )), texts = LyricsLine(listOf(
            text("abc", x = 0f),
        )))
        assertThat(wrapped).containsExactly(
            LyricsLine(listOf(
                LyricsFragment.text("abc", x = 0f, width = 3f),
            )),
            LyricsLine(listOf(
                LyricsFragment.chords("A", x = 0f, width = 1f),
            )),
        )
    }

    @Test
    fun test_very_long_words() {
        val screenWRelative = 5f
        val wrapper = DoubleLineWrapper(screenWRelative, equalLengthMapper)
        val wrapped = wrapper.wrapDoubleLine(chords = LyricsLine(listOf(
            chord("AFCGAFCG", x = 0f),
        )), texts = LyricsLine(listOf(
            text("abcdefgeH", x = 0f),
        )))
        assertThat(wrapped).containsExactly(
            LyricsLine(listOf(
                chord("AFCGAFCG", x = 0f),
            )),
            LyricsLine(listOf(
                text("abcdefgeH", x = 0f),
            )),
        )
    }

    @Test
    fun test_very_long_words_single() {
        val wrapper = DoubleLineWrapper(screenWRelative = 5f, lengthMapper = equalLengthMapper)
        val wrapped = wrapper.wrapDoubleLine(chords = LyricsLine(listOf(
        )), texts = LyricsLine(listOf(
            text("abcdefgeH", x = 0f),
        )))
        assertThat(wrapped).containsExactly(
            LyricsLine(listOf(
                text("abcdefgeH", x = 0f),
            )),
        )
    }

}