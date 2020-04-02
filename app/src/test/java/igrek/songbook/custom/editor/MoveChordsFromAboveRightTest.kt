package igrek.songbook.custom.editor


import android.widget.EditText
import igrek.songbook.dagger.base.BaseDaggerTest
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito

class MoveChordsFromAboveRightTest : BaseDaggerTest() {

    private val transformer = ChordsEditorTransformer(
            contentEdit = Mockito.mock(EditText::class.java),
            history = Mockito.mock(LyricsEditorHistory::class.java),
            chordsNotation = ChordsNotation.GERMAN,
            uiResourceService = Mockito.mock(UiResourceService::class.java),
            uiInfoService = Mockito.mock(UiInfoService::class.java),
    )

    @Test
    fun test_transformMoveChordsAboveToRight() {
        val lyrics = """
            [a F]
            first line
            [C G]
            second line
            """.trimIndent()
        val transformed = transformer.transformMoveChordsAboveToRight(lyrics)
        val expected = """
            first line [a F]
            second line [C G]
            """.trimIndent()
        assertThat(transformed).isEqualTo(expected)
        assertThat(transformer.transformMoveChordsAboveToRight(expected)).isEqualTo(expected)
    }

    @Test
    fun test_many_chords_with_sharp_or_numbers() {
        val lyrics = """
            [g Gmaj7/C(sus4)]
            esencja istnienia
            [D# B c Dsus4-A, C#/G]
            second line
            """.trimIndent()
        val transformed = transformer.transformMoveChordsAboveToRight(lyrics)
        val expected = """
            esencja istnienia [g Gmaj7/C(sus4)]
            second line [D# B c Dsus4-A, C#/G]
            """.trimIndent()
        assertThat(transformed).isEqualTo(expected)
    }

    @Test
    fun test_move_chords_from_above_leave_dont_inline_unclear() {
        val lyrics = """
            [a]inlined[F]
            first line
            """.trimIndent()
        val transformed = transformer.transformMoveChordsAboveToRight(lyrics)
        assertThat(transformed).isEqualTo(lyrics)
    }

    @Test
    fun test_move_chords_from_above_multipart() {
        val lyrics = """
            [a] [F C]
            first line
            """.trimIndent()
        val transformed = transformer.transformMoveChordsAboveToRight(lyrics)
        val expected = """
            first line [a] [F C]
            """.trimIndent()
        assertThat(transformed).isEqualTo(expected)
    }

    @Test
    fun test_dont_move_from_above_if_already_chrods() {
        val lyrics = """
            [a]
            first line [F]
            """.trimIndent()
        val transformed = transformer.transformMoveChordsAboveToRight(lyrics)
        assertThat(transformed).isEqualTo(lyrics)
    }

}
