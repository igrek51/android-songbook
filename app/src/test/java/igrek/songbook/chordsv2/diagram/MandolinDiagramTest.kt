package igrek.songbook.chordsv2.diagram

import igrek.songbook.settings.instrument.ChordsInstrument
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MandolinDiagramTest {

    private val horizontalBuilder = ChordDiagramBuilder(ChordsInstrument.MANDOLIN, ChordDiagramStyle.Horizontal)
    private val verticalBuilder = ChordDiagramBuilder(ChordsInstrument.MANDOLIN, ChordDiagramStyle.Vertical)

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
