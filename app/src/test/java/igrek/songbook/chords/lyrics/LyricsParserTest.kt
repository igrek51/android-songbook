package igrek.songbook.chords.lyrics

import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsTextType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LyricsParserTest {

    @Test
    fun test_parse_lyrics_and_mark_chords() {
        val model = LyricsParser().parseContent("""
        dupa [a F C7/G(-A) G]
        [D]next li[e]ne [C]
        
           next [a]verse [G]    
        without chords
        
        
        """.trimIndent())
        assertThat(model.lines).hasSize(5)
        assertThat(model.lines).isEqualTo(listOf(
                LyricsLine(listOf(
                        LyricsFragment(text = "dupa ", type = LyricsTextType.REGULAR_TEXT),
                        LyricsFragment(text = "a F C7/G(-A) G", type = LyricsTextType.CHORDS),
                )),
                LyricsLine(listOf(
                        LyricsFragment(text = "D", type = LyricsTextType.CHORDS),
                        LyricsFragment(text = "next li", type = LyricsTextType.REGULAR_TEXT),
                        LyricsFragment(text = "e", type = LyricsTextType.CHORDS),
                        LyricsFragment(text = "ne ", type = LyricsTextType.REGULAR_TEXT),
                        LyricsFragment(text = "C", type = LyricsTextType.CHORDS),
                )),
                LyricsLine(listOf()),
                LyricsLine(listOf(
                        LyricsFragment(text = "next ", type = LyricsTextType.REGULAR_TEXT),
                        LyricsFragment(text = "a", type = LyricsTextType.CHORDS),
                        LyricsFragment(text = "verse ", type = LyricsTextType.REGULAR_TEXT),
                        LyricsFragment(text = "G", type = LyricsTextType.CHORDS),
                )),
                LyricsLine(listOf(
                        LyricsFragment(text = "without chords", type = LyricsTextType.REGULAR_TEXT),
                ))
        ))
    }

    @Test
    fun test_parse_many_brackets() {
        val model = LyricsParser().parseContent("""
        [[a ]]b[
        c
        ]
        """.trimIndent())

        assertThat(model.lines).isEqualTo(listOf(
                LyricsLine(listOf(
                        LyricsFragment(text = "a ", type = LyricsTextType.CHORDS),
                        LyricsFragment(text = "b", type = LyricsTextType.REGULAR_TEXT)
                )),
                LyricsLine(listOf(
                        LyricsFragment(text = "c", type = LyricsTextType.CHORDS),
                ))
        ))
    }

    @Test
    fun test_parse_without_trimming() {
        val model = LyricsParser(trimWhitespaces = false).parseContent("""
           a  [a]
        bcde
        """.trimIndent())

        assertThat(model.lines).isEqualTo(listOf(
                LyricsLine(listOf(
                        LyricsFragment(text = "   a  ", type = LyricsTextType.REGULAR_TEXT),
                        LyricsFragment(text = "a", type = LyricsTextType.CHORDS)
                )),
                LyricsLine(listOf(
                        LyricsFragment(text = "bcde", type = LyricsTextType.REGULAR_TEXT),
                ))
        ))
    }
}