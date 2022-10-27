package igrek.songbook.kotlin

import org.junit.Test
import kotlin.test.assertEquals

class StringTest {

    @Test
    fun test_null_stringer() {
        val empty: CharSequence? = null
        assertEquals(empty.toString(), "null")
        assertEquals(empty?.toString().orEmpty(), "")
    }

}
