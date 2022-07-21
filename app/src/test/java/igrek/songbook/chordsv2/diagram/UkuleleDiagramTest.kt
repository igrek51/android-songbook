package igrek.songbook.chordsv2.diagram

import igrek.songbook.settings.instrument.ChordsInstrument
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UkuleleDiagramTest {

    private val horizontalBuilder = ChordDiagramBuilder(ChordsInstrument.UKULELE, ChordDiagramStyle.Horizontal)
    private val verticalBuilder = ChordDiagramBuilder(ChordsInstrument.UKULELE, ChordDiagramStyle.Vertical)

    @Test
    fun buildHorizontalDiagram() {
        assertThat(horizontalBuilder.buildDiagram("x,3,2,0")).isEqualTo("""
                A 0|-|-|-|
                E  |-|2|-|
                C  |-|-|3|
                G x|-|-|-|
                """.trimIndent())
    }

    @Test
    fun buildVerticalDiagram() {
        assertThat(verticalBuilder.buildDiagram("x,3,2,0")).isEqualTo("""
                G C E A
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
