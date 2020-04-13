package igrek.songbook.chords.diagram

import igrek.songbook.settings.instrument.ChordsInstrument
import org.junit.Test

class AllDiagramBuildTest {

    private val guitarBuilder = ChordDiagramBuilder(ChordsInstrument.GUITAR)
    private val ukuleleBuilder = ChordDiagramBuilder(ChordsInstrument.UKULELE)
    private val mandolinBuilder = ChordDiagramBuilder(ChordsInstrument.MANDOLIN)

    @Test
    fun test_all_guitar_diagrams_build() {
        allGuitarChordsDiagrams.forEach { (_, diagrams) ->
            diagrams.forEach { diagram ->
                guitarBuilder.buildDiagram(diagram)
            }
        }
    }

    @Test
    fun test_all_ukulele_diagrams_build() {
        allUkuleleChordsDiagrams.forEach { (_, diagrams) ->
            diagrams.forEach { diagram ->
                ukuleleBuilder.buildDiagram(diagram)
            }
        }
    }

    @Test
    fun test_all_mandolin_diagrams_build() {
        allMandolinChordsDiagrams.forEach { (_, diagrams) ->
            diagrams.forEach { diagram ->
                mandolinBuilder.buildDiagram(diagram)
            }
        }
    }
}