package igrek.songbook.chords.diagram

import igrek.songbook.chords.diagram.guitar.ChordDiagramStyle
import igrek.songbook.chords.diagram.guitar.ChordTextDiagramBuilder
import igrek.songbook.settings.instrument.ChordsInstrument
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MandolinDiagramTest {

    private val horizontalBuilder = ChordTextDiagramBuilder(ChordsInstrument.MANDOLIN, ChordDiagramStyle.Horizontal)
    private val verticalBuilder = ChordTextDiagramBuilder(ChordsInstrument.MANDOLIN, ChordDiagramStyle.Vertical)

    @Test
    fun buildHorizontalDiagram() {
        assertThat(horizontalBuilder.buildDiagram("x,3,2,0")).isEqualTo("""
                E 0|-|-|-|
                A  |-|2|-|
                D  |-|-|3|
                G x|-|-|-|
                """.trimIndent())
    }


    @Test
    fun buildVerticalDiagram() {
        assertThat(verticalBuilder.buildDiagram("x,3,2,0")).isEqualTo("""
                G D A E
                x     0
                -------
                | | | |
                -------
                | | 2 |
                -------
                | 3 | |
                -------
                """.trimIndent())
    }

}
