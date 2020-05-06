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

}
