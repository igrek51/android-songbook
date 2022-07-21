package igrek.songbook.chords.converter

import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ChordsNotationConverterTest {

    @Test
    fun test_convertToSame() {
        var converter = ChordsNotationConverter(ChordsNotation.GERMAN, ChordsNotation.GERMAN)
        var input = "du [a F# f7] p a [Gmaj7 G#]\n[B Hsus4]"
        assertThat(converter.convertLyrics(input, originalModifiers = true)).isEqualTo(input)

        converter = ChordsNotationConverter(ChordsNotation.GERMAN_IS, ChordsNotation.GERMAN_IS)
        input = "du [a Fis7 f7] p a [Gmaj7 Gis]\n[B Hsus4]"
        assertThat(converter.convertLyrics(input, originalModifiers = true)).isEqualTo(input)

        converter = ChordsNotationConverter(ChordsNotation.ENGLISH, ChordsNotation.ENGLISH)
        input = "du [Am F# Fm7] p a [Gmaj7 G#]\n[Bb Hsus4]"
        assertThat(converter.convertLyrics(input, originalModifiers = true)).isEqualTo(input)
    }

    @Test
    fun test_unknownChords() {
        val converter = ChordsNotationConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        val input = "[unknown a dupa h Dsus2]"
        assertThat(converter.convertLyrics(input)).isEqualTo("[unknown Am dupa Bm Dsus2]")
    }

    @Test
    fun test_convertGermanToEnglish() {
        val german2english = ChordsNotationConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        val english2german = ChordsNotationConverter(ChordsNotation.ENGLISH, ChordsNotation.GERMAN)
        val german = "lyrics [a b B Asus4 d7 f# g#7 C-G7-a a/G]\n[h H H7]"
        val english = "lyrics [Am Bbm Bb Asus4 Dm7 F#m G#m7 C-G7-Am Am/G]\n[Bm B B7]"
        assertThat(german2english.convertLyrics(german)).isEqualTo(english)
        assertThat(english2german.convertLyrics(english)).isEqualTo(german)
    }

    @Test
    fun test_convertChordSharpMoll() {
        val english2german = ChordsNotationConverter(ChordsNotation.ENGLISH, ChordsNotation.GERMAN)
        assertThat(english2german.convertChordFragments("Gm")).isEqualTo("g")
        assertThat(english2german.convertChordFragments("G#", originalModifiers = true)).isEqualTo("G#")
        assertThat(english2german.convertChordFragments("G#m", originalModifiers = true)).isEqualTo("g#")
        assertThat(english2german.convertChordFragments("G#m", originalModifiers = false)).isEqualTo("ab")
        assertThat(english2german.convertChordFragments("G#m7", originalModifiers = true)).isEqualTo("g#7")
    }

    @Test
    fun test_convertCompoundChord() {
        val english2german = ChordsNotationConverter(ChordsNotation.ENGLISH, ChordsNotation.GERMAN)
        assertThat(english2german.convertChordFragments("G#m/A", originalModifiers = true)).isEqualTo("g#/A")
        assertThat(english2german.convertChordFragments("dupa", originalModifiers = true)).isEqualTo("dupa")
    }

    @Test
    fun test_tooLongChordName() {
        val english2german = ChordsNotationConverter(ChordsNotation.ENGLISH, ChordsNotation.GERMAN)
        val input = "[G#m7sus4add9+]"
        assertThat(english2german.convertLyrics(input)).isEqualTo(input)
    }

    @Test
    fun test_convertParts() {
        val converter = ChordsNotationConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        assertThat(converter.convertLyrics("lyrics [a b B h] [Asus4 d7 f#]")).
                isEqualTo("lyrics [Am Bbm Bb Bm] [Asus4 Dm7 F#m]")
        assertThat(converter.convertChordFragments("a")).isEqualTo("Am")
    }

    @Test
    fun test_Fmaj7() {
        val converter = ChordsNotationConverter(ChordsNotation.ENGLISH, ChordsNotation.GERMAN)
        assertThat(converter.convertLyrics("[Fmaj7]")).isEqualTo("[Fmaj7]")
    }

    @Test
    fun test_lyrics_to_english() {
        val converter = ChordsNotationConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        assertThat(converter.convertLyrics("a [a b B h]")).isEqualTo("a [Am Bbm Bb Bm]")
    }

    @Test
    fun test_lyrics_with_newlines() {
        val converter = ChordsNotationConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        assertThat(converter.convertLyrics("a\n[a\nb\nB\nh]", originalModifiers = true)).isEqualTo("a\n[Am]\n[Bbm]\n[Bb]\n[Bm]")
    }

    @Test
    fun test_mutliple_groups() {
        val converter = ChordsNotationConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        assertThat(converter.convertLyrics("[a] txt [f c]\n[d]")).isEqualTo("[Am] txt [Fm Cm]\n[Dm]")
    }

    @Test
    fun test_fix_default_variation() {
        val converter = ChordsNotationConverter(ChordsNotation.GERMAN_IS, ChordsNotation.GERMAN_IS)
        assertThat(converter.convertLyrics("[c#]")).isEqualTo("[cis]")
    }

    @Test
    fun test_convert_to_simpler_form() {
        val converter = ChordsNotationConverter(ChordsNotation.GERMAN_IS, ChordsNotation.GERMAN)
        assertThat(converter.convertLyrics("[D#]")).isEqualTo("[Eb]")
    }

    @Test
    fun test_convert_german_moll_to_english() {
        val converter = ChordsNotationConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
        assertThat(converter.convertLyrics("[e]")).isEqualTo("[Em]")
    }

    @Test
    fun test_convert_slashed_chord() {
        val converter = ChordsNotationConverter(ChordsNotation.ENGLISH, ChordsNotation.GERMAN)
        assertThat(converter.convertLyrics("[C/B]")).isEqualTo("[C/H]")
    }

}
