package igrek.songbook.chords.diagram

import igrek.songbook.settings.instrument.ChordsInstrument
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerticalChordsDiagramsTest {

    private val builder = ChordDiagramBuilder(ChordsInstrument.GUITAR, ChordDiagramStyle.Vertical)

    @Test
    fun test_build_C_dur() {
        assertThat(builder.buildDiagram("x,3,2,0,1,0")).isEqualTo("""
                E A D G H E
                x     0   0
                -----------
                | | | | 1 |
                -----------
                | | 2 | | |
                -----------
                | 3 | | | |
                -----------
                """.trimIndent())
    }

    @Test
    fun test_build_F_bar() {
        assertThat(builder.buildDiagram("1,3,3,2,1,1")).isEqualTo("""
                E A D G H E
                           
                -----------
                1 | | | 1 1
                -----------
                | | | 2 | |
                -----------
                | 3 3 | | |
                -----------
                """.trimIndent())
    }

    @Test
    fun test_build_Fis_bar() {
        assertThat(builder.buildDiagram("2,4,4,3,2,2")).isEqualTo("""
                E A D G H E
                … … … … … …
                -----------
                2 | | | 2 2
                -----------
                | | | 3 | |
                -----------
                | 4 4 | | |
                -----------
                """.trimIndent())
    }

    @Test
    fun test_build_a_bar() {
        assertThat(builder.buildDiagram("5,7,7,6,5,5")).isEqualTo("""
                E A D G H E
                … … … … … …
                -----------
                5 | | | 5 5
                -----------
                | | | 6 | |
                -----------
                | 7 7 | | |
                -----------
                """.trimIndent())
    }

    @Test
    fun test_empty_strings() {
        assertThat(builder.buildDiagram("0,0,0,0,0,0")).isEqualTo("""
                E A D G H E
                0 0 0 0 0 0
                -----------
                | | | | | |
                -----------
                | | | | | |
                -----------
                | | | | | |
                -----------
                """.trimIndent())
    }

    @Test
    fun test_empty_and_muted_with_high_notes() {
        // 51
        assertThat(builder.buildDiagram("x,5,7,7,0,0")).isEqualTo("""
                E A D G H E
                x … … … 0 0
                -----------
                | 5 | | | |
                -----------
                | | | | | |
                -----------
                | | 7 7 | |
                -----------
                """.trimIndent())
    }

    @Test
    fun test_2_digit_notes() {
        assertThat(builder.buildDiagram("x,10,11,12,0,0")).isEqualTo("""
                E  A  D  G  H  E
                x  …  …  …  0  0
                ----------------
                |  10 |  |  |  |
                ----------------
                |  |  11 |  |  |
                ----------------
                |  |  |  12 |  |
                ----------------
                """.trimIndent())
    }

    @Test
    fun test_one_fret_on_3() {
        assertThat(builder.buildDiagram("x,x,0,0,0,3")).isEqualTo("""
                E A D G H E
                x x 0 0 0  
                -----------
                | | | | | |
                -----------
                | | | | | |
                -----------
                | | | | | 3
                -----------
                """.trimIndent())
    }

    @Test
    fun test_first_ellipsis() {
        assertThat(builder.buildDiagram("x,x,0,0,0,4")).isEqualTo("""
                E A D G H E
                x x 0 0 0 …
                -----------
                | | | | | 4
                -----------
                | | | | | |
                -----------
                | | | | | |
                -----------
                """.trimIndent())
    }

    @Test
    fun test_long_handed_ellipsis() {
        assertThat(builder.buildDiagram("x,x,0,10,12,15")).isEqualTo("""
                E  A  D  G  H  E
                x  x  0  …  …  …
                ----------------
                |  |  |  10 |  |
                ----------------
                |  |  |  |  |  |
                ----------------
                |  |  |  |  12 |
                ----------------
                |  |  |  |  |  |
                ----------------
                |  |  |  |  |  |
                ----------------
                |  |  |  |  |  15
                ----------------
                """.trimIndent())
    }

    @Test
    fun test_mixed_digits_count() {
        assertThat(builder.buildDiagram("x,x,0,9,11,11")).isEqualTo("""
                E  A  D  G  H  E
                x  x  0  …  …  …
                ----------------
                |  |  |  9  |  |
                ----------------
                |  |  |  |  |  |
                ----------------
                |  |  |  |  11 11
                ----------------
                """.trimIndent())
    }

}
