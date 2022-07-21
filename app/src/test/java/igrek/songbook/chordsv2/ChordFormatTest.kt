package igrek.songbook.chordsv2

import igrek.songbook.chordsv2.model.*
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ChordFormatTest {

    @Test
    fun test_formatChordToString() {
        val notation = ChordsNotation.ENGLISH
        assertThat(Chord(0, false, "maj7").format(notation)).isEqualTo("Cmaj7")
        assertThat(Chord(1, true, "").format(notation)).isEqualTo("C#m")
        assertThat(Chord(1, true, "").format(ChordsNotation.GERMAN)).isEqualTo("c#")
        assertThat(Chord(1, false, "").format(notation, NoteModifier.SHARP)).isEqualTo("C#")
        assertThat(Chord(1, false, "").format(notation, NoteModifier.FLAT)).isEqualTo("Db")

        assertThat(Chord(10, false, "").format(ChordsNotation.GERMAN)).isEqualTo("B")
        assertThat(Chord(10, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("Bb")
        assertThat(Chord(11, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("B")
        assertThat(Chord(8, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("Ab")
        assertThat(Chord(3, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("Eb")
        assertThat(Chord(1, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("C#")
    }



}