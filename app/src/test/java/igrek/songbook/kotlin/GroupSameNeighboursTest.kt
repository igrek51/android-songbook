package igrek.songbook.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GroupSameNeighboursTest {

    @Test
    fun test_group_same_consecutive_neighbours() {
        val chars: List<Char> = "AACCCAC".toList()

        val groups = chars.groupConsecutiveDuplicates { char -> char }

        assertThat(groups).containsExactly(
                listOf('A', 'A'),
                listOf('C', 'C', 'C'),
                listOf('A'),
                listOf('C'),
        )
    }

    @Test
    fun test_group_same_consecutive_neighbours2() {
        val chars: List<Char> = "AACCCAC".toList()

        val groups = chars.fold(mutableListOf<MutableList<Char>>()) { lists, c ->
            if (lists.isEmpty() || lists.last().last() != c) {
                lists += mutableListOf(c) // add new group
            } else {
                lists.last() += c // add to the last group
            }
            lists
        }

        assertThat(groups).containsExactly(
                mutableListOf('A', 'A'),
                mutableListOf('C', 'C', 'C'),
                mutableListOf('A'),
                mutableListOf('C'),
        )
    }

}

internal fun <T, C> Iterable<T>.groupConsecutiveDuplicates(comparisonSelector: (T) -> C): List<List<T>> =
        mutableListOf<MutableList<T>>().also { lists ->
            forEach {
                if (lists.isEmpty() || comparisonSelector(lists.last().last()) != comparisonSelector(it))
                    lists += mutableListOf(it) // add new group
                else
                    lists.last() += it // add to the last group
            }
        }
