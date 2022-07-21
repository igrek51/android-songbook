package igrek.songbook.chords.diagram

import igrek.songbook.chords.diagram.guitar.ChordTextDiagramBuilder
import igrek.songbook.settings.instrument.ChordsInstrument
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AllDiagramBuildTest {

    private val horizontalGuitarBuilder = ChordTextDiagramBuilder(ChordsInstrument.GUITAR)
    private val horizontalUkuleleBuilder = ChordTextDiagramBuilder(ChordsInstrument.UKULELE)
    private val horizontalMandolinBuilder = ChordTextDiagramBuilder(ChordsInstrument.MANDOLIN)
    private val verticalGuitarBuilder = ChordTextDiagramBuilder(ChordsInstrument.GUITAR)
    private val verticalUkuleleBuilder = ChordTextDiagramBuilder(ChordsInstrument.UKULELE)
    private val verticalMandolinBuilder = ChordTextDiagramBuilder(ChordsInstrument.MANDOLIN)

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