package igrek.songbook.custom.editor


import igrek.songbook.dagger.base.BaseDaggerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ChordsEditorMoveChordsFromAboveTest : BaseDaggerTest() {

    @Test
    fun test_transformMoveChordsAboveToRight() {
        val chordsEditorLayourController = ChordsEditorLayoutController()
        val lyrics = """
            [a F]
            first line
            [C G]
            second line
            """.trimIndent()
        val transformed = chordsEditorLayourController.transformMoveChordsAboveToRight(lyrics)
        val expected = """
            first line [a F]
            second line [C G]
            """.trimIndent()
        assertThat(transformed).isEqualTo(expected)
    }

    @Test
    fun test_move_chords_from_above_leave_dont_inline_unclear() {
        val chordsEditorLayourController = ChordsEditorLayoutController()
        val lyrics = """
            [a]inlined[F]
            first line
            """.trimIndent()
        val transformed = chordsEditorLayourController.transformMoveChordsAboveToRight(lyrics)
        assertThat(transformed).isEqualTo(lyrics)
    }

    @Test
    fun test_move_chords_from_above_multipart() {
        val chordsEditorLayourController = ChordsEditorLayoutController()
        val lyrics = """
            [a] [F C]
            first line
            """.trimIndent()
        val transformed = chordsEditorLayourController.transformMoveChordsAboveToRight(lyrics)
        val expected = """
            first line [a] [F C]
            """.trimIndent()
        assertThat(transformed).isEqualTo(expected)
    }

}
