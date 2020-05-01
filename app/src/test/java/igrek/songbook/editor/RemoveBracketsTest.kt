package igrek.songbook.editor

import igrek.songbook.inject.SingletonInject
import igrek.songbook.mock.UiInfoServiceMock
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito


class RemoveBracketsTest {

    private val textEditor = EmptyTextEditor()

    private val transformer = ChordsEditorTransformer(
            history = Mockito.mock(LyricsEditorHistory::class.java),
            chordsNotation = ChordsNotation.GERMAN,
            textEditor = textEditor,
            uiInfoService = SingletonInject { UiInfoServiceMock() },
    )

    @Test
    fun removeBracketsContent() {
        textEditor.setText("""
        [Chorus]
        text
        and [Solo] more
        1[abc3 Cmja7/G(A-H)
        Hmb]2
        3 [lazy] 5 [matcher] 7 
        """.trimIndent())
        transformer.removeBracketsContent()
        assertThat(textEditor.getText()).isEqualTo("""
        
        text
        and  more
        12
        3  5  7 
        """.trimIndent())
    }

    @Test
    fun unmarkChords() {
        textEditor.setText("""
        [Chorus]
        text
        and [Solo] more
        1[abc3 Cmja7/G(A-H)
        Hmb]2
        3 [lazy] 5 [matcher] 7 
        """.trimIndent())
        transformer.unmarkChords()
        assertThat(textEditor.getText()).isEqualTo("""
        Chorus
        text
        and Solo more
        1abc3 Cmja7/G(A-H)
        Hmb2
        3 lazy 5 matcher 7 
        """.trimIndent())
    }

}