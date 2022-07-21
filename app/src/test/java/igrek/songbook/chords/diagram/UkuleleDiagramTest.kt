package igrek.songbook.chords.diagram

import igrek.songbook.chords.diagram.guitar.ChordDiagramStyle
import igrek.songbook.chords.diagram.guitar.ChordTextDiagramBuilder
import igrek.songbook.settings.instrument.ChordsInstrument
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UkuleleDiagramTest {

    private val horizontalBuilder = ChordTextDiagramBuilder(ChordsInstrument.UKULELE, ChordDiagramStyle.Horizontal)
    private val verticalBuilder = ChordTextDiagramBuilder(ChordsInstrument.UKULELE, ChordDiagramStyle.Vertical)

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
