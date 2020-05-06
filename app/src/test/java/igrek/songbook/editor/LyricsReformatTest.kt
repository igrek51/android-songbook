package igrek.songbook.editor

import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.SingletonInject
import igrek.songbook.mock.ClipboardManagerMock
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions
import org.junit.Test
import org.mockito.Mockito

class LyricsReformatTest {

    private val transformer = ChordsEditorTransformer(
            history = Mockito.mock(LyricsEditorHistory::class.java),
            chordsNotation = ChordsNotation.GERMAN,
            textEditor = EmptyTextEditor(),
            uiInfoService = SingletonInject { Mockito.mock(UiInfoService::class.java) },
            clipboardManager = SingletonInject { ClipboardManagerMock() },
    )

    @Test
    fun test_reformat() {
        val lyrics = """
        foo   [a] [F][C]  bar   
        """.trimIndent()
        val transformed = transformer.reformatAndTrim(lyrics)
        val expected = """
        foo [a F C] bar
        """.trimIndent()
        Assertions.assertThat(transformed).isEqualTo(expected)
        Assertions.assertThat(transformer.reformatAndTrim(expected)).isEqualTo(expected)
    }

    @Test
    fun lastChordsPadding() {
        val lyrics = """
        bar[a]
        foo[C] bar[a]
        [B]in-[F]words [G]
        end[a]
        """.trimIndent()
        val transformed = transformer.reformatAndTrim(lyrics)
        val expected = """
        bar [a]
        foo [C] bar [a]
        [B]in-[F]words [G]
        end [a]
        """.trimIndent()
        Assertions.assertThat(transformed).isEqualTo(expected)
        Assertions.assertThat(transformer.reformatAndTrim(expected)).isEqualTo(expected)
    }

    @Test
    fun doubleSpaceInChords() {
        val lyrics = """
        [a F  C G]
        [a F   C G]
        words  words
        """.trimIndent()
        val transformed = transformer.reformatAndTrim(lyrics)
        val expected = """
        [a F  C G]
        [a F  C G]
        words words
        """.trimIndent()
        Assertions.assertThat(transformed).isEqualTo(expected)
        Assertions.assertThat(transformer.reformatAndTrim(expected)).isEqualTo(expected)
    }

}