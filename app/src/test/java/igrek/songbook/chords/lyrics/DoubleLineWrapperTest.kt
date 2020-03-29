package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.wrapper.DoubleLineWrapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DoubleLineWrapperTest {

    @Test
    fun test_unwrapped_short() {
        val wrapper = DoubleLineWrapper(screenWRelative = 10f, lengthMapper = equalLengthMapper)
        val wrapped = wrapper.wrapDoubleLine(chords = LyricsLine(
                chord("A", x = 0f),
        ), texts = LyricsLine(
                text("jolka", x = 0f),
        ))
        assertThat(wrapped).containsExactly(
                LyricsLine(
                        LyricsFragment.Chord("A", x = 0f, width = 1f),
                ),
                LyricsLine(
                        LyricsFragment.Text("jolka", x = 0f, width = 5f),
                ),
        )
    }

    @Test
    fun test_wrapped_join_adjacent() {
        val wrapper = DoubleLineWrapper(screenWRelative = 10f, lengthMapper = equalLengthMapper)
        val wrapped = wrapper.wrapDoubleLine(chords = LyricsLine(
                chord("A", x = 0f),
                chord("F", x = 2f),
                chord("C", x = 7f),
        ), texts = LyricsLine(
                text("jolka", x = 0f),
                text("jolka", x = 5f),
                text("a", x = 10f),
        ))
        assertThat(wrapped).containsExactly(
                LyricsLine(
                        LyricsFragment.Chord("A", x = 0f, width = 1f),
                        LyricsFragment.Chord("F", x = 2f, width = 1f),
                        LyricsFragment.Chord("C", x = 7f, width = 1f),
                ),
                LyricsLine(
                        LyricsFragment.Text("jolkajolka", x = 0f, width = 10f),
                        linewrapper(10f),
                ),
                LyricsLine(
                        LyricsFragment.Text("a", x = 0f, width = 1f),
                )
        )
    }

    @Test
    fun test_wrapped_long_chords() {
        val wrapper = DoubleLineWrapper(screenWRelative = 7f, lengthMapper = equalLengthMapper)
        val wrapped = wrapper.wrapDoubleLine(chords = LyricsLine(
                chord("C D E F G A H C", x = 0f),
        ), texts = LyricsLine(
                text("jolka", x = 0f),
        ))
        assertThat(wrapped).containsExactly(
                LyricsLine(
                        LyricsFragment.Chord("C D E ", x = 0f, width = 6f),
                        linewrapper(7f),
                ),
                LyricsLine(
                        LyricsFragment.Text("jolka", x = 0f, width = 5f),
                ),
                LyricsLine(
                        LyricsFragment.Chord("F G A ", x = 0f, width = 6f),
                        linewrapper(7f),
                ),
                LyricsLine(
                        LyricsFragment.Chord("H C", x = 0f, width = 3f),
                ),
        )
    }

    @Test
    fun test_next_line_moved_to_align() {
        val wrapper = DoubleLineWrapper(screenWRelative = 10f, lengthMapper = equalLengthMapper)
        val wrapped = wrapper.wrapDoubleLine(chords = LyricsLine(
                chord("C", x = 12f),
        ), texts = LyricsLine(
                text("jolkajolk ", x = 0f),
                text("jolka", x = 10f),
        ))
        assertThat(wrapped).containsExactly(
                LyricsLine(
                        LyricsFragment.Text("jolkajolk ", x = 0f, width = 10f),
                        linewrapper(10f),
                ),
                LyricsLine(
                        LyricsFragment.Chord("C", x = 2f, width = 1f),
                ),
                LyricsLine(
                        LyricsFragment.Text("jolka", x = 0f, width = 5f),
                ),
        )
    }

}