package igrek.songbook.editor

import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.junit.Test
import org.mockito.Mockito

class ChordsEditorTransformerTest {

    private val transformer = ChordsEditorTransformer(
            history = Mockito.mock(LyricsEditorHistory::class.java),
            chordsNotation = ChordsNotation.GERMAN,
            uiResourceService = Mockito.mock(UiResourceService::class.java),
            uiInfoService = Mockito.mock(UiInfoService::class.java),
            textEditor = EmptyTextEditor(),
    )

    @Test
    fun test_transformMoveChordsAboveToRight() {

    }
}