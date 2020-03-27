package igrek.songbook.chords.lyrics

import android.graphics.Typeface
import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.lyrics.model.LyricsTextType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test

class LyricsInflaterTest {

    @Test
    @Ignore("cant instantiate any Typeface")
    fun test_inflate_lyrics() {
        val model = LyricsModel(listOf(
                LyricsLine(listOf(
                        LyricsFragment(text = "dupa", type = LyricsTextType.REGULAR_TEXT),
                        LyricsFragment(text = "a F", type = LyricsTextType.CHORDS),
                )),
                LyricsLine(listOf(
                        LyricsFragment(text = "i", type = LyricsTextType.REGULAR_TEXT),
                        LyricsFragment(text = "m", type = LyricsTextType.REGULAR_TEXT),
                        LyricsFragment(text = "i", type = LyricsTextType.CHORDS),
                        LyricsFragment(text = "m", type = LyricsTextType.CHORDS),
                ))
        ))

        val inflated = LyricsInflater(Typeface.MONOSPACE, fontsize = 10f).inflateLyrics(model)
        assertThat(inflated.lines).hasSize(2)
        assertThat(model.lines).isEqualTo(listOf(
                LyricsLine(listOf(
                        LyricsFragment(x = 0f, width = 40f, text = "dupa", type = LyricsTextType.REGULAR_TEXT),
                        LyricsFragment(x = 40f, width = 30f, text = "a F", type = LyricsTextType.CHORDS),
                )),
                LyricsLine(listOf(
                        LyricsFragment(x = 0f, width = 10f, text = "i", type = LyricsTextType.REGULAR_TEXT),
                        LyricsFragment(x = 10f, width = 10f, text = "m", type = LyricsTextType.REGULAR_TEXT),
                        LyricsFragment(x = 20f, width = 10f, text = "i", type = LyricsTextType.CHORDS),
                        LyricsFragment(x = 30f, width = 10f, text = "m", type = LyricsTextType.CHORDS),
                ))
        ))
    }

}