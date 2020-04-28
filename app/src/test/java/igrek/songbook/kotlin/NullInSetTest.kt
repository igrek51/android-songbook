package igrek.songbook.kotlin

import org.junit.Test
import kotlin.test.assertTrue

class NullInSetTest {

    @Test
    fun test() {
        val list = listOf("something") + "" + null
        assertTrue { "" in list }
        assertTrue { null in list }
    }

}
