package igrek.songbook.chords.detector

import igrek.songbook.settings.instrument.ChordsInstrument
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UniqueChordsFinderTest {

    @Test
    fun test_whiter_shade_of_pale_unique_chords() {
        val instrument: ChordsInstrument = ChordsInstrument.default
        val chordsFinder = UniqueChordsFinder(instrument)
        val chordFragments = listOf(
                "C C/H a a/G",
                "F F/E d d/C",
                "G G/F e e/D",
                "C F G F G",
                "C C/H a a/G",
                "F F/E d d/C",
                "G G/F e e/D",
                "C C/H a a/G",
                "F F/E d d/C",
                "G G/F e e/D",
                "C C/H a a/G",
                "F F/E d",
                "G7 C C/H a a/G",
                "F F/E d d/C",
                "G G/F e e/D",
                "C F C G6add11 G6"
        )
        val uniqueChords = chordsFinder.findUniqueChords(chordFragments)

        assertThat(uniqueChords).containsOnly(
                "C", "C/H", "a", "a/G",
                "F", "F/E", "d", "d/C",
                "G", "G/F", "e", "e/D",
                "G6add11", "G6", "G7"
        )
    }

}
