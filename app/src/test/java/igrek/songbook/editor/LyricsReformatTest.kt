package igrek.songbook.editor

import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.SingletonInject
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
    }

}