package igrek.songbook.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GroupSameNeighboursTest {

    @Test
    fun test_group_same_consecutive_neighbours() {
        val chars: List<Char> = "AACCCAC".toList()
        val groups = chars.groupBy { char -> char }
                .map { (key, grouped) ->
                    grouped
                }

        assertThat(groups).containsExactly(
                listOf('A', 'A', 'A'),
                listOf('C', 'C', 'C', 'C'),
        )
    }

}
