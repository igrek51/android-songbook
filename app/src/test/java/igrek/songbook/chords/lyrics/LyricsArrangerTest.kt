package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.lyrics.model.LyricsTextType
import igrek.songbook.settings.theme.DisplayStyle
import org.assertj.core.api.Assertions
import org.junit.Test

class LyricsArrangerTest {

    private val lengthMapper = TypefaceLengthMapper(
            ' ' to 1f,
            'a' to 1f,
            'b' to 1f,
            'c' to 1f,
            'm' to 2f,
            'F' to 1f,
    )

    @Test
    fun test_chords_inline_untouched() {
        val wrapper = LyricsArranger(displayStyle = DisplayStyle.ChordsInline,
                screenWRelative = 10f, lengthMapper = lengthMapper)
        val wrapped = wrapper.arrangeModel(LyricsModel(
                LyricsLine(
                        LyricsFragment(text = "a b", type = LyricsTextType.REGULAR_TEXT, width = 3f),
                        LyricsFragment(text = "a F", type = LyricsTextType.CHORDS, width = 3f),
                        LyricsFragment(text = "c", type = LyricsTextType.REGULAR_TEXT, width = 1f),
                )
        ))
        Assertions.assertThat(wrapped.lines).containsExactly(
                LyricsLine(
                        LyricsFragment(text = "c d", type = LyricsTextType.REGULAR_TEXT, width = 5f),
                )
        )
    }

}