package igrek.songbook.editor

import igrek.songbook.inject.SingletonInject
import igrek.songbook.mock.ClipboardManagerMock
import igrek.songbook.mock.UiInfoServiceMock
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito


class ChordsMarkingTest {

    private val textEditor = EmptyTextEditor()

    private val transformer = ChordsEditorTransformer(
            history = Mockito.mock(LyricsEditorHistory::class.java),
            chordsNotation = ChordsNotation.GERMAN,
            textEditor = textEditor,
            uiInfoService = SingletonInject { UiInfoServiceMock() },
            clipboardManager = SingletonInject { ClipboardManagerMock() },
    )

    @Test
    fun test_detectChords() {
        textEditor.setText("""
            F    G
            word work Csus4 D
            a is a [e]
            """.trimIndent())
        transformer.detectChords()
        assertThat(textEditor.getText()).isEqualTo("""
            [F]    [G]
            word work [Csus4] [D]
            a is [a] [e]
            """.trimIndent())
    }

    @Test
    fun test_detectChords_keeping_indentation() {
        textEditor.setText("""
            F    Gsus4 a   
            word work  work
            """.trimIndent())
        transformer.detectChords(keepIndentation = true)
        assertThat(textEditor.getText()).isEqualTo("""
            [F]  [Gsus4][a] 
            word work  work
            """.trimIndent())
    }

    @Test
    fun test_detect_complex_chords() {
        textEditor.setText("""
            Asus4-C(G)-F/G E
            """.trimIndent())
        transformer.detectChords()
        assertThat(textEditor.getText()).isEqualTo("""
            [Asus4-C(G)-F/G] [E]
            """.trimIndent())
    }
}