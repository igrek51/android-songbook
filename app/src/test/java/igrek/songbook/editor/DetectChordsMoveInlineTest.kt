package igrek.songbook.editor


import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.SingletonInject
import igrek.songbook.mock.ClipboardManagerMock
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito

class DetectChordsMoveInlineTest {

    private val textEditor = EmptyTextEditor()

    private val transformer = ChordsEditorTransformer(
            history = Mockito.mock(LyricsEditorHistory::class.java),
            chordsNotation = ChordsNotation.GERMAN,
            textEditor = textEditor,
            uiInfoService = SingletonInject { Mockito.mock(UiInfoService::class.java) },
            clipboardManager = SingletonInject { ClipboardManagerMock() },
    )

    @Test
    fun detectAndMoveChordsAboveToInline() {
        textEditor.setText("""
            F    Cmaj7 Dsus4   a   B C
            word workk work inside-a-word
            [a]
            already marked
            a
            already chords [a]
            """.trimIndent())
        transformer.detectAndMoveChordsAboveToInline()
        assertThat(textEditor.getText()).isEqualTo("""
            [F]word [Cmaj7]workk [Dsus4]work ins[a]ide-[B]a-[C]word
            [a]already marked
            [a]
            already chords [a]
            """.trimIndent())
    }

    @Test
    fun improveUnalignedChords() {
        textEditor.setText("""
             F   Cmaj7   Dsus4 G
            This is the world  world
            """.trimIndent())
        transformer.detectAndMoveChordsAboveToInline()
        assertThat(textEditor.getText()).isEqualTo("""
            [F]This [Cmaj7]is the [Dsus4]world  [G]world
            """.trimIndent())
    }

    @Test
    fun doubledLinesWithChords() {
        textEditor.setText("""
            e D a
            e D a
            [e D a] [e D a]
            [e D a]
            """.trimIndent())
        transformer.detectAndMoveChordsAboveToInline()
        assertThat(textEditor.getText()).isEqualTo("""
            [e] [D] [a]
            [e] [D] [a]
            [e D a] [e D a]
            [e D a]
            """.trimIndent())
    }

    @Test
    fun markedAndUnmarked() {
        textEditor.setText("""
            F    Cmaj7 Dsus4
            word workk work
            [a] [h]  [C]
            alr ady  marked
            """.trimIndent())
        transformer.detectAndMoveChordsAboveToInline()
        assertThat(textEditor.getText()).isEqualTo("""
            [F]word [Cmaj7]workk [Dsus4]work
            [a]alr [h]ady  [C]marked
            """.trimIndent())
    }

}
