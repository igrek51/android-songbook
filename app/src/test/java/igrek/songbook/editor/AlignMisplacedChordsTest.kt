package igrek.songbook.editor

import igrek.songbook.inject.SingletonInject
import igrek.songbook.mock.ClipboardManagerMock
import igrek.songbook.mock.UiInfoServiceMock
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions
import org.junit.Test
import org.mockito.Mockito

class AlignMisplacedChordsTest {

    private val textEditor = EmptyTextEditor()

    private val transformer = ChordsEditorTransformer(
        history = Mockito.mock(LyricsEditorHistory::class.java),
        chordsNotationProvider = { ChordsNotation.GERMAN },
        textEditor = textEditor,
        uiInfoService = SingletonInject { UiInfoServiceMock() },
        clipboardManager = SingletonInject { ClipboardManagerMock() },
    )

    @Test
    fun alignMisplacedChords() {
        textEditor.setText("""
            i[a]nvalid in[G]valid invali[E]d
            [F]valid [C]valid [C#]valid
            """.trimIndent())
        transformer.alignMisplacedChords()
        Assertions.assertThat(textEditor.getText()).isEqualTo("""
            [a]invalid [G]invalid [E]invalid
            [F]valid [C]valid [C#]valid
            """.trimIndent())
    }

    @Test
    fun alignManyChordsInWord() {
        textEditor.setText("""
            many[a]many[F]many
            """.trimIndent())
        transformer.alignMisplacedChords()
        Assertions.assertThat(textEditor.getText()).isEqualTo("""
            [a]manymany[F]many
            """.trimIndent())

        textEditor.setText("""
            [a]manymany[F]many
            """.trimIndent())
        transformer.alignMisplacedChords()
        Assertions.assertThat(textEditor.getText()).isEqualTo("""
            [a][F]manymanymany
            """.trimIndent())
    }

}