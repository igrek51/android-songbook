package igrek.songbook.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RegexTest {

    @Test
    fun regexReplace() {
        val txt = "chords]\nwords  words["
        var counter = 0
        txt.replace(Regex("""]([\S\s]*?)\[""")) { matchResult ->
            val inside = matchResult.groupValues[1]
            counter++
            "]$inside["
        }
        assertThat(counter).isEqualTo(1)
    }

    @Test
    fun regexMatch() {
        val reg = Regex("""-----BEGIN-SONGBOOK-KEY-----([\S\s]+?)-----END-SONGBOOK-KEY-----""")
        val txt = "-----BEGIN-SONGBOOK-KEY-----\n" +
                "    dupa\n" +
                "    -----END-SONGBOOK-KEY-----"

        val match = reg.matchEntire(txt.trim())
        assertThat(match).isNotNull
        match?.let {
            val encodedCommand = it.groupValues[1].trim().lines()[0]
            assertThat(encodedCommand.trim()).isEqualTo("dupa")
        }
    }

}
