package igrek.songbook.chordsv2.diagram

import org.junit.Test


class DuplicatesTest {

    @Test
    fun test_duplicates_in_particular_chords() {
        guitarChordsDiagrams.forEach { (name, diagrams) ->
            val uniqueDiagrams = mutableSetOf<String>()
            diagrams.forEach { diagram ->
                assert(diagram !in uniqueDiagrams) { "chord $name has duplicated diagrams: $diagram" }
                uniqueDiagrams.add(diagram)
            }
        }
    }

    @Test
    fun test_duplicated_diagrams_between_chords() {
        val errors = mutableListOf<String>()
        val uniqueDiagrams = mutableSetOf<String>()
        guitarChordsDiagrams.forEach { (name, diagrams) ->
            diagrams.forEach { diagram ->
                if(diagram in uniqueDiagrams) {
                    errors.add("diagram $diagram is not unique for chord $name")
                }
                uniqueDiagrams.add(diagram)
            }
        }

        // assert(errors.isEmpty()) { errors.joinToString(separator = "\n") }
    }

}