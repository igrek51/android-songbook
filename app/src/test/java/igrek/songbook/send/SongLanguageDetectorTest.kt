package igrek.songbook.send

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SongLanguageDetectorTest {

    @Test
    fun detectLanguageCodeEn() {
        val lang = SongLanguageDetector().detectLanguageCode("So, so you think you can tell?")
        assertThat(lang).isEqualTo("en")
    }

    @Test
    fun detectLanguageCodePl() {
        val lang = SongLanguageDetector().detectLanguageCode("Nie płacz Ewka...")
        assertThat(lang).isEqualTo("pl")
    }
}