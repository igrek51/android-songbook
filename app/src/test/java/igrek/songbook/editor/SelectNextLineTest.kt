package igrek.songbook.editor

import igrek.songbook.inject.SingletonInject
import igrek.songbook.mock.UiInfoServiceMock
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito


class SelectNextLineTest {

    private val textEditor = EmptyTextEditor()

    private val transformer = ChordsEditorTransformer(
            history = Mockito.mock(LyricsEditorHistory::class.java),
            chordsNotation = ChordsNotation.GERMAN,
            textEditor = textEditor,
            uiInfoService = SingletonInject { UiInfoServiceMock() },
    )

    @Test
    fun test_select_whole_text() {
        textEditor.setText("lines")
        textEditor.setSelection(2, 2)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(0 to 5)
    }

    @Test
    fun test_select_next_line() {
        textEditor.setText("line\nnext")
        textEditor.setSelection(2, 2)

        transformer.selectNextLine()
        val (start, end) = textEditor.getSelection()
        assertThat(start).isEqualTo(0)
        assertThat(end).isEqualTo(5)

        transformer.selectNextLine()
        val (start2, end2) = textEditor.getSelection()
        assertThat(start2).isEqualTo(0)
        assertThat(end2).isEqualTo(9)
    }

    @Test
    fun test_select_empty_line() {
        textEditor.setText("\n\nline")
        textEditor.setSelection(0, 0)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(0 to 1)

        textEditor.setSelection(1, 1)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(1 to 2)
    }

    @Test
    fun test_select_next_empty_line() {
        textEditor.setText("\n\nline\nnext")
        textEditor.setSelection(0, 1)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(0 to 2)

        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(0 to 7)
    }

    @Test
    fun test_select_non_empty_lines() {
        textEditor.setText("\nab\ncd\nef")
        textEditor.setSelection(2, 2)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(1 to 4)

        textEditor.setSelection(1, 1)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(1 to 4)

        textEditor.setSelection(3, 3)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(1 to 4)

        textEditor.setSelection(1, 2)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(1 to 4)

        textEditor.setSelection(1, 3)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(1 to 4)

        textEditor.setSelection(2, 3)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(1 to 4)
    }

    @Test
    fun test_select_next_non_empty_lines() {
        textEditor.setText("\nab\ncd\nef")
        textEditor.setSelection(1, 4)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(1 to 7)

        textEditor.setSelection(1, 5)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(1 to 7)

        textEditor.setSelection(1, 6)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(1 to 7)

        textEditor.setSelection(1, 7)
        transformer.selectNextLine()
        assertThat(textEditor.getSelection()).isEqualTo(1 to 9)
    }

}