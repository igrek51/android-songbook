package igrek.songbook.about.secret

import org.junit.Test
import kotlin.test.assertEquals

class ShaHasherTest {

    @Test
    fun test_single_sha256() {
        val hash = ShaHasher().singleHash("dupa")
        assertEquals(hash,
                "60a5d3e4100fe8afa5ee0103739a45711d50d7f3ba7280d8a95b51f5d04aa4b8")
    }

}
