package igrek.songbook.chordsv2.diagram

import igrek.songbook.settings.instrument.ChordsInstrument
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AllDiagramBuildTest {

    private val horizontalGuitarBuilder = ChordDiagramBuilder(ChordsInstrument.GUITAR)
    private val horizontalUkuleleBuilder = ChordDiagramBuilder(ChordsInstrument.UKULELE)
    private val horizontalMandolinBuilder = ChordDiagramBuilder(ChordsInstrument.MANDOLIN)
    private val verticalGuitarBuilder = ChordDiagramBuilder(ChordsInstrument.GUITAR)
    private val verticalUkuleleBuilder = ChordDiagramBuilder(ChordsInstrument.UKULELE)
    private val verticalMandolinBuilder = ChordDiagramBuilder(ChordsInstrument.MANDOLIN)

    @Test
    fun test_all_guitar_diagrams_build() {
        allGuitarChordsDiagrams.forEach { (_, diagrams) ->
            diagrams.forEach { diagram ->
                horizontalGuitarBuilder.buildDiagram(diagram)
                verticalGuitarBuilder.buildDiagram(diagram)
            }
        }
    }

    @Test
    fun test_all_ukulele_diagrams_build() {
        allUkuleleChordsDiagrams.forEach { (_, diagrams) ->
            diagrams.forEach { diagram ->
                horizontalUkuleleBuilder.buildDiagram(diagram)
                verticalUkuleleBuilder.buildDiagram(diagram)
            }
        }
    }

    @Test
    fun test_all_mandolin_diagrams_build() {
        allMandolinChordsDiagrams.forEach { (_, diagrams) ->
            diagrams.forEach { diagram ->
                horizontalMandolinBuilder.buildDiagram(diagram)
                verticalMandolinBuilder.buildDiagram(diagram)
            }
        }
    }

    @Test
    fun containsAliasChords() {
        assertThat(allGuitarChordsDiagrams.keys).contains("Bb7")
        assertThat(allGuitarChordsDiagrams.keys).contains("A#7")
    }
}