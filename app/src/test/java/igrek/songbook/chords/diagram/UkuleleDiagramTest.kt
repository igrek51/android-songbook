package igrek.songbook.chords.diagram

import igrek.songbook.settings.instrument.ChordsInstrument
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UkuleleDiagramTest {

    private val builder = ChordDiagramBuilder(ChordsInstrument.UKULELE)

    @Test
    fun test_build_ukulele_chord() {
        assertThat(builder.buildDiagram("x,3,2,0")).isEqualTo("""
                A 0|-|-|-|
                E  |-|2|-|
                C  |-|-|3|
                G x|-|-|-|
                """.trimIndent())
    }

}
