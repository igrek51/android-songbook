package igrek.songbook.chords

import org.assertj.core.api.Assertions.assertThat
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.junit.Test

class ChordsConverterTest {

    @Test
    fun test_convertToSame() {
        var converter = ChordsConverter(ChordsNotation.GERMAN, ChordsNotation.GERMAN)
        var input = "du [a F# f7] p a [Gmaj7 G#]\n[B Hsus4]"
        assertThat(converter.convertLyrics(input)).isEqualTo(input)

        converter = ChordsConverter(ChordsNotation.GERMAN_IS, ChordsNotation.GERMAN_IS)
        input = "du [a Fis7 f7] p a [Gmaj7 Gis]\n[B Hsus4]"
        assertThat(converter.convertLyrics(input)).isEqualTo(input)

        converter = ChordsConverter(ChordsNotation.ENGLISH, ChordsNotation.ENGLISH)
        input = "du [Am F# Fm7] p a [Gmaj7 G#]\n[Bb Hsus4]"
        assertThat(converter.convertLyrics(input)).isEqualTo(input)
    }

    @Test
    fun test_unknownChords() {
        val converter = ChordsConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        val input = "[unknown a dupa h Dsus2]"
        assertThat(converter.convertLyrics(input)).isEqualTo("[unknown Am dupa Bm Dsus2]")
    }

    @Test
    fun test_convertGermanToEnglish() {
        val german2english = ChordsConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        val english2german = ChordsConverter(ChordsNotation.ENGLISH, ChordsNotation.GERMAN)
        val german = "lyrics [a b B Asus4 d7 f# g#7 C-G7-a a/G]\n[h H H7]"
        val english = "lyrics [Am Bbm Bb Asus4 Dm7 F#m G#m7 C-G7-Am Am/G]\n[Bm B B7]"
        assertThat(german2english.convertLyrics(german)).isEqualTo(english)
        assertThat(english2german.convertLyrics(english)).isEqualTo(german)
    }

    @Test
    fun test_convertChordSharpMoll() {
        val english2german = ChordsConverter(ChordsNotation.ENGLISH, ChordsNotation.GERMAN)
        assertThat(english2german.convertChord("Gm")).isEqualTo("g")
        assertThat(english2german.convertChord("G#")).isEqualTo("G#")
        assertThat(english2german.convertChord("G#m")).isEqualTo("g#")
        assertThat(english2german.convertChord("G#m7")).isEqualTo("g#7")
    }

    @Test
    fun test_tooLongChordName() {
        val english2german = ChordsConverter(ChordsNotation.ENGLISH, ChordsNotation.GERMAN)
        val input = "[G#m7sus4add9+]"
        assertThat(english2german.convertLyrics(input)).isEqualTo(input)
    }

    @Test
    fun test_convertParts() {
        val converter = ChordsConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        assertThat(converter.convertLyrics("lyrics [a b B h] [Asus4 d7 f#]")).
                isEqualTo("lyrics [Am Bbm Bb Bm] [Asus4 Dm7 F#m]")
        assertThat(converter.convertChord("a")).isEqualTo("Am")
    }

    @Test
    fun test_Fmaj7() {
        val converter = ChordsConverter(ChordsNotation.ENGLISH, ChordsNotation.GERMAN)
        assertThat(converter.convertLyrics("[Fmaj7]")).isEqualTo("[Fmaj7]")
    }

    @Test
    fun test_lyrics_to_english() {
        val converter = ChordsConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        assertThat(converter.convertLyrics("a [a b B h]")).isEqualTo("a [Am Bbm Bb Bm]")
    }

    @Test
    fun test_lyrics_with_newlines() {
        val converter = ChordsConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        assertThat(converter.convertLyrics("a\n[a\nb\nB\nh]")).isEqualTo("a\n[Am\nBbm\nBb\nBm]")
    }

    @Test
    fun test_mutliple_groups() {
        val converter = ChordsConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        assertThat(converter.convertLyrics("[a] txt [f c]\n[d]")).isEqualTo("[Am] txt [Fm Cm]\n[Dm]")
    }

    @Test
    fun test_fix_default_variation() {
        val converter = ChordsConverter(ChordsNotation.GERMAN_IS, ChordsNotation.GERMAN_IS)
        assertThat(converter.convertLyrics("[c#]")).isEqualTo("[cis]")
    }

    @Test
    fun test_convert_german_moll_to_english() {
        val converter = ChordsConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        assertThat(converter.convertLyrics("[e]")).isEqualTo("[Em]")
    }

}
