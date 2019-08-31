package igrek.songbook.chords

import igrek.songbook.chords.diagram.ChordDiagramBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ChordsDiagramsTest {

    private val builder = ChordDiagramBuilder()

    @Test
    fun test_build_C_dur() {
        assertThat(builder.buildDiagram("x,3,2,0,1,0")).isEqualTo("""
                E 0|-|-|-|
                H  |1|-|-|
                G 0|-|-|-|
                D  |-|2|-|
                A  |-|-|3|
                E x|-|-|-|
                """.trimIndent())
    }

    @Test
    fun test_build_F_bar() {
        assertThat(builder.buildDiagram("1,3,3,2,1,1")).isEqualTo("""
                E  |1|-|-|
                H  |1|-|-|
                G  |-|2|-|
                D  |-|-|3|
                A  |-|-|3|
                E  |1|-|-|
                """.trimIndent())
    }

    @Test
    fun test_build_Fis_bar() {
        assertThat(builder.buildDiagram("2,4,4,3,2,2")).isEqualTo("""
                E …|2|-|-|
                H …|2|-|-|
                G …|-|3|-|
                D …|-|-|4|
                A …|-|-|4|
                E …|2|-|-|
                """.trimIndent())
    }

    @Test
    fun test_build_a_bar() {
        assertThat(builder.buildDiagram("5,7,7,6,5,5")).isEqualTo("""
                E …|5|-|-|
                H …|5|-|-|
                G …|-|6|-|
                D …|-|-|7|
                A …|-|-|7|
                E …|5|-|-|
                """.trimIndent())
    }

    @Test
    fun test_empty_strings() {
        assertThat(builder.buildDiagram("0,0,0,0,0,0")).isEqualTo("""
                E 0|-|-|-|
                H 0|-|-|-|
                G 0|-|-|-|
                D 0|-|-|-|
                A 0|-|-|-|
                E 0|-|-|-|
                """.trimIndent())
    }

    @Test
    fun test_empty_and_muted_with_high_notes() {
        // 51
        assertThat(builder.buildDiagram("x,5,7,7,0,0")).isEqualTo("""
                E 0|-|-|-|
                H 0|-|-|-|
                G …|-|-|7|
                D …|-|-|7|
                A …|5|-|-|
                E x|-|-|-|
                """.trimIndent())
    }

    @Test
    fun test_2_digit_notes() {
        // 51
        assertThat(builder.buildDiagram("x,10,11,12,0,0")).isEqualTo("""
                E 0|--|--|--|
                H 0|--|--|--|
                G …|--|--|12|
                D …|--|11|--|
                A …|10|--|--|
                E x|--|--|--|
                """.trimIndent())
    }

}
