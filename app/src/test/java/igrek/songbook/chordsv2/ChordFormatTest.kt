package igrek.songbook.chordsv2

import igrek.songbook.chordsv2.model.*
import igrek.songbook.chordsv2.parser.ChordParser
import igrek.songbook.chordsv2.parser.LyricsParser
import igrek.songbook.chordsv2.syntax.MajorKey
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
        assertThat(Chord(1, false, "").format(notation, MajorKey.D_MAJOR)).isEqualTo("C#")
        assertThat(Chord(1, false, "").format(notation, MajorKey.A_FLAT_MAJOR)).isEqualTo("Db")

        assertThat(Chord(10, false, "").format(ChordsNotation.GERMAN)).isEqualTo("B")
        assertThat(Chord(10, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("Bb")
        assertThat(Chord(11, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("B")
        assertThat(Chord(8, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("Ab")
        assertThat(Chord(3, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("Eb")
        assertThat(Chord(1, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("C#")
    }

    @Test
    fun test_parseAndFormatLyricsAndChords() {
        val lyrics = LyricsParser().parseLyrics("""
        dupa [a F C7/G G]
        [D]next li[e]ne [C]
        
           next [a]verse [G]    
        without chords
        
        
        """.trimIndent())

        val chordParser = ChordParser(ChordsNotation.GERMAN)
        chordParser.parseAndFillChords(lyrics)

        assertThat(lyrics.lines).hasSize(5)
        assertThat(lyrics.lines).isEqualTo(listOf(
            LyricsLine(listOf(
                LyricsFragment(text = "dupa ", type = LyricsTextType.REGULAR_TEXT),
                LyricsFragment(text = "a F C7/G G", type = LyricsTextType.CHORDS, chordFragments = listOf(
                    ChordFragment(text="a", type=ChordFragmentType.SINGLE_CHORD, singleChord=Chord(Note.A.index, true)),
                    ChordFragment(text=" ", type=ChordFragmentType.CHORD_SPLITTER),
                    ChordFragment(text="F", type=ChordFragmentType.SINGLE_CHORD, singleChord=Chord(Note.F.index, false)),
                    ChordFragment(text=" ", type=ChordFragmentType.CHORD_SPLITTER),
                    ChordFragment(text="C7/G", type=ChordFragmentType.COMPOUND_CHORD, compoundChord = CompoundChord(
                        Chord(Note.C.index, false, "7"), "/",  Chord(Note.G.index, false),
                    )),
                    ChordFragment(text=" ", type=ChordFragmentType.CHORD_SPLITTER),
                    ChordFragment(text="G", type=ChordFragmentType.SINGLE_CHORD, singleChord=Chord(Note.G.index, false)),
                )),
            )),
            LyricsLine(listOf(
                LyricsFragment(text = "D", type = LyricsTextType.CHORDS, chordFragments = listOf(
                    ChordFragment(text="D", type=ChordFragmentType.SINGLE_CHORD, singleChord=Chord(Note.D.index, false)),
                )),
                LyricsFragment(text = "next li", type = LyricsTextType.REGULAR_TEXT),
                LyricsFragment(text = "e", type = LyricsTextType.CHORDS, chordFragments = listOf(
                    ChordFragment(text="e", type=ChordFragmentType.SINGLE_CHORD, singleChord=Chord(Note.E.index, true)),
                )),
                LyricsFragment(text = "ne ", type = LyricsTextType.REGULAR_TEXT),
                LyricsFragment(text = "C", type = LyricsTextType.CHORDS, chordFragments = listOf(
                    ChordFragment(text="C", type=ChordFragmentType.SINGLE_CHORD, singleChord=Chord(Note.C.index, false)),
                )),
            )),
            LyricsLine(listOf()),
            LyricsLine(listOf(
                LyricsFragment(text = "next ", type = LyricsTextType.REGULAR_TEXT),
                LyricsFragment(text = "a", type = LyricsTextType.CHORDS, chordFragments = listOf(
                    ChordFragment(text="a", type=ChordFragmentType.SINGLE_CHORD, singleChord=Chord(Note.A.index, true)),
                )),
                LyricsFragment(text = "verse ", type = LyricsTextType.REGULAR_TEXT),
                LyricsFragment(text = "G", type = LyricsTextType.CHORDS, chordFragments = listOf(
                    ChordFragment(text="G", type=ChordFragmentType.SINGLE_CHORD, singleChord=Chord(Note.G.index, false)),
                )),
            )),
            LyricsLine(listOf(
                LyricsFragment(text = "without chords", type = LyricsTextType.REGULAR_TEXT),
            ))
        ))

        val formatter = ChordsFormatter(ChordsNotation.ENGLISH)
        formatter.formatLyrics(lyrics)

        assertThat(lyrics.lines[0]).isEqualTo(LyricsLine(listOf(
            LyricsFragment(text = "dupa ", type = LyricsTextType.REGULAR_TEXT),
            LyricsFragment(text = "Am F C7/G G", type = LyricsTextType.CHORDS, chordFragments = listOf(
                ChordFragment(text="Am", type=ChordFragmentType.SINGLE_CHORD, singleChord=Chord(Note.A.index, true)),
                ChordFragment(text=" ", type=ChordFragmentType.CHORD_SPLITTER),
                ChordFragment(text="F", type=ChordFragmentType.SINGLE_CHORD, singleChord=Chord(Note.F.index, false)),
                ChordFragment(text=" ", type=ChordFragmentType.CHORD_SPLITTER),
                ChordFragment(text="C7/G", type=ChordFragmentType.COMPOUND_CHORD, compoundChord = CompoundChord(
                    Chord(Note.C.index, false, "7"), "/",  Chord(Note.G.index, false),
                )),
                ChordFragment(text=" ", type=ChordFragmentType.CHORD_SPLITTER),
                ChordFragment(text="G", type=ChordFragmentType.SINGLE_CHORD, singleChord=Chord(Note.G.index, false)),
            )),
        )))
    }


    @Test
    fun test_formatWithKey() {
        val lyrics = LyricsParser().parseLyrics("""text [D/Ab G#m]""".trimIndent())
        ChordParser(ChordsNotation.ENGLISH).parseAndFillChords(lyrics)
        ChordsFormatter(ChordsNotation.ENGLISH, key=MajorKey.E_MAJOR).formatLyrics(lyrics)

        assertThat(lyrics.lines[0]).isEqualTo(LyricsLine(listOf(
            LyricsFragment(text = "text ", type = LyricsTextType.REGULAR_TEXT),
            LyricsFragment(text = "D/G# G#m", type = LyricsTextType.CHORDS, chordFragments = listOf(
                ChordFragment(text="D/G#", type=ChordFragmentType.COMPOUND_CHORD, compoundChord = CompoundChord(
                    Chord(Note.D.index, false),
                    "/",
                    Chord(Note.G_SHARP.index, false, "", originalModifier = NoteModifier.FLAT),
                )),
                ChordFragment(text=" ", type=ChordFragmentType.CHORD_SPLITTER),
                ChordFragment(text="G#m", type=ChordFragmentType.SINGLE_CHORD, singleChord=Chord(Note.G_SHARP.index, true, "", NoteModifier.SHARP)),
            )),
        )))

        ChordsFormatter(ChordsNotation.ENGLISH, key=MajorKey.E_FLAT_MAJOR).formatLyrics(lyrics)
        assertThat(lyrics.lines[0]).isEqualTo(LyricsLine(listOf(
            LyricsFragment(text = "text ", type = LyricsTextType.REGULAR_TEXT),
            LyricsFragment(text = "D/Ab Abm", type = LyricsTextType.CHORDS, chordFragments = listOf(
                ChordFragment(text="D/Ab", type=ChordFragmentType.COMPOUND_CHORD, compoundChord = CompoundChord(
                    Chord(Note.D.index, false),
                    "/",
                    Chord(Note.A_FLAT.index, false, "", originalModifier = NoteModifier.FLAT),
                )),
                ChordFragment(text=" ", type=ChordFragmentType.CHORD_SPLITTER),
                ChordFragment(text="Abm", type=ChordFragmentType.SINGLE_CHORD, singleChord=Chord(Note.A_FLAT.index, true, "", originalModifier = NoteModifier.SHARP)),
            )),
        )))
    }

}