package igrek.songbook.chords.diagram

import igrek.songbook.settings.instrument.ChordsInstrument
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MandolinDiagramTest {

    private val builder = ChordDiagramBuilder(ChordsInstrument.MANDOLIN)

    @Test
    fun test_build_mandolin_chord() {
        assertThat(builder.buildDiagram("x,3,2,0")).isEqualTo("""
                E 0|-|-|-|
                A  |-|2|-|
                D  |-|-|3|
                G x|-|-|-|
                """.trimIndent())
    }

}
