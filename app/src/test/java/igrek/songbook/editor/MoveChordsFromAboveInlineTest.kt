package igrek.songbook.editor


import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.SingletonInject
import igrek.songbook.mock.ClipboardManagerMock
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito

class MoveChordsFromAboveInlineTest {

    private val transformer = ChordsEditorTransformer(
            history = Mockito.mock(LyricsEditorHistory::class.java),
            chordsNotation = ChordsNotation.GERMAN,
            textEditor = EmptyTextEditor(),
            uiInfoService = SingletonInject { Mockito.mock(UiInfoService::class.java) },
            clipboardManager = SingletonInject { ClipboardManagerMock() },
    )

    @Test
    fun test_move_single_chords() {
        val lyrics = """
            [a]   [F]
            first line
            [C]    [G]
            second line
            """.trimIndent()
        val transformed = transformer.transformMoveChordsAboveToInline(lyrics)
        val expected = """
            [a]first [F]line
            [C]second [G]line
            """.trimIndent()
        assertThat(transformed).isEqualTo(expected)
        assertThat(transformer.transformMoveChordsAboveToInline(expected)).isEqualTo(expected)
    }

    @Test
    fun test_move_double_chords() {
        val lyrics = """
            [a F]
            first line
            """.trimIndent()
        val transformed = transformer.transformMoveChordsAboveToInline(lyrics)
        val expected = """
            [a F]first line
            """.trimIndent()
        assertThat(transformed).isEqualTo(expected)
        assertThat(transformer.transformMoveChordsAboveToInline(expected)).isEqualTo(expected)
    }

    @Test
    fun test_dont_move_when_already_chords() {
        val lyrics = """
            [a]
            first line [F]
            second line
            third line
            """.trimIndent()
        val transformed = transformer.transformMoveChordsAboveToInline(lyrics)
        assertThat(transformed).isEqualTo(lyrics)
    }

    @Test
    fun test_move_inside_word() {
        val lyrics = """
                [a] [F]
            veryLongWord
            """.trimIndent()
        val transformed = transformer.transformMoveChordsAboveToInline(lyrics)
        val expected = """
            very[a]Long[F]Word
            """.trimIndent()
        assertThat(transformed).isEqualTo(expected)
    }

    @Test
    fun test_chords_longer_than_word() {
        val lyrics = """
              [C]  [a] [F]
            s ort
            """.trimIndent()
        val transformed = transformer.transformMoveChordsAboveToInline(lyrics)
        val expected = """
            s [C]ort  [a]    [F]
            """.trimIndent()
        assertThat(transformed).isEqualTo(expected)
    }

    @Test
    fun test_remove_double_empty_lines() {
        val transformed = transformer.transformRemoveDoubleEmptyLines("""
abc
   
def


ghi
""")
        assertThat(transformed).isEqualTo("""
abc
def

ghi
""")
    }

}
