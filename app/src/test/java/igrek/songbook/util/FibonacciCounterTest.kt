package igrek.songbook.util

import org.junit.Test
import kotlin.test.assertEquals

class FibonacciCounterTest {
    @Test
    fun testFibonacciCounterNext() {
        val counter = FibonacciCounter()
        assertEquals(1, counter.next())
        assertEquals(1, counter.next())
        assertEquals(2, counter.next())
        assertEquals(3, counter.next())
        assertEquals(5, counter.next())
        assertEquals(8, counter.next())
        assertEquals(13, counter.next())
        counter.reset()
        assertEquals(1, counter.next())
        assertEquals(1, counter.next())
        assertEquals(2, counter.next())
    }
}