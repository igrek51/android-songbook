package igrek.songbook.chords.render

import igrek.songbook.chords.model.*
import igrek.songbook.chords.parser.ChordParser
import igrek.songbook.chords.parser.LyricsParser
import igrek.songbook.chords.syntax.MajorKey
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions
import org.junit.Test

class ChordFormatTest {

    @Test
    fun test_formatChordToString() {
        val notation = ChordsNotation.ENGLISH
        Assertions.assertThat(Chord(0, false, "maj7").format(notation)).isEqualTo("Cmaj7")
        Assertions.assertThat(Chord(1, true, "").format(notation)).isEqualTo("C#m")
        Assertions.assertThat(Chord(1, true, "").format(ChordsNotation.GERMAN)).isEqualTo("c#")
        Assertions.assertThat(Chord(1, false, "").format(notation, MajorKey.D_MAJOR)).isEqualTo("C#")
        Assertions.assertThat(Chord(1, false, "").format(notation, MajorKey.A_FLAT_MAJOR)).isEqualTo("Db")

        Assertions.assertThat(Chord(10, false, "").format(ChordsNotation.GERMAN)).isEqualTo("B")
        Assertions.assertThat(Chord(10, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("Bb")
        Assertions.assertThat(Chord(11, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("B")
        Assertions.assertThat(Chord(8, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("Ab")
        Assertions.assertThat(Chord(3, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("Eb")
        Assertions.assertThat(Chord(1, false, "").format(ChordsNotation.ENGLISH)).isEqualTo("C#")
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

        Assertions.assertThat(lyrics.lines).hasSize(5)
        Assertions.assertThat(lyrics.lines).isEqualTo(listOf(
            LyricsLine(
                listOf(
                    LyricsFragment(text = "dupa ", type = LyricsTextType.REGULAR_TEXT),
                    LyricsFragment(
                        text = "a F C7/G G", type = LyricsTextType.CHORDS, chordFragments = listOf(
                            ChordFragment(
                                text = "a",
                                type = ChordFragmentType.SINGLE_CHORD,
                                singleChord = Chord(Note.A.index, true)
                            ),
                            ChordFragment(text = " ", type = ChordFragmentType.CHORD_SPLITTER),
                            ChordFragment(
                                text = "F",
                                type = ChordFragmentType.SINGLE_CHORD,
                                singleChord = Chord(Note.F.index, false)
                            ),
                            ChordFragment(text = " ", type = ChordFragmentType.CHORD_SPLITTER),
                            ChordFragment(
                                text = "C7/G",
                                type = ChordFragmentType.COMPOUND_CHORD,
                                compoundChord = CompoundChord(
                                    Chord(Note.C.index, false, "7"),
                                    "/",
                                    Chord(Note.G.index, false),
                                )
                            ),
                            ChordFragment(text = " ", type = ChordFragmentType.CHORD_SPLITTER),
                            ChordFragment(
                                text = "G",
                                type = ChordFragmentType.SINGLE_CHORD,
                                singleChord = Chord(Note.G.index, false)
                            ),
                        )
                    ),
                )
            ),
            LyricsLine(
                listOf(
                    LyricsFragment(
                        text = "D", type = LyricsTextType.CHORDS, chordFragments = listOf(
                            ChordFragment(
                                text = "D",
                                type = ChordFragmentType.SINGLE_CHORD,
                                singleChord = Chord(Note.D.index, false)
                            ),
                        )
                    ),
                    LyricsFragment(text = "next li", type = LyricsTextType.REGULAR_TEXT),
                    LyricsFragment(
                        text = "e", type = LyricsTextType.CHORDS, chordFragments = listOf(
                            ChordFragment(
                                text = "e",
                                type = ChordFragmentType.SINGLE_CHORD,
                                singleChord = Chord(Note.E.index, true)
                            ),
                        )
                    ),
                    LyricsFragment(text = "ne ", type = LyricsTextType.REGULAR_TEXT),
                    LyricsFragment(
                        text = "C", type = LyricsTextType.CHORDS, chordFragments = listOf(
                            ChordFragment(
                                text = "C",
                                type = ChordFragmentType.SINGLE_CHORD,
                                singleChord = Chord(Note.C.index, false)
                            ),
                        )
                    ),
                )
            ),
            LyricsLine(listOf()),
            LyricsLine(
                listOf(
                    LyricsFragment(text = "next ", type = LyricsTextType.REGULAR_TEXT),
                    LyricsFragment(
                        text = "a", type = LyricsTextType.CHORDS, chordFragments = listOf(
                            ChordFragment(
                                text = "a",
                                type = ChordFragmentType.SINGLE_CHORD,
                                singleChord = Chord(Note.A.index, true)
                            ),
                        )
                    ),
                    LyricsFragment(text = "verse ", type = LyricsTextType.REGULAR_TEXT),
                    LyricsFragment(
                        text = "G", type = LyricsTextType.CHORDS, chordFragments = listOf(
                            ChordFragment(
                                text = "G",
                                type = ChordFragmentType.SINGLE_CHORD,
                                singleChord = Chord(Note.G.index, false)
                            ),
                        )
                    ),
                )
            ),
            LyricsLine(
                listOf(
                    LyricsFragment(text = "without chords", type = LyricsTextType.REGULAR_TEXT),
                )
            )
        ))

        val formatter = ChordsRenderer(ChordsNotation.ENGLISH)
        formatter.formatLyrics(lyrics)

        Assertions.assertThat(lyrics.lines[0]).isEqualTo(
            LyricsLine(
                listOf(
                    LyricsFragment(text = "dupa ", type = LyricsTextType.REGULAR_TEXT),
                    LyricsFragment(
                        text = "Am F C7/G G", type = LyricsTextType.CHORDS, chordFragments = listOf(
                            ChordFragment(
                                text = "Am",
                                type = ChordFragmentType.SINGLE_CHORD,
                                singleChord = Chord(Note.A.index, true)
                            ),
                            ChordFragment(text = " ", type = ChordFragmentType.CHORD_SPLITTER),
                            ChordFragment(
                                text = "F",
                                type = ChordFragmentType.SINGLE_CHORD,
                                singleChord = Chord(Note.F.index, false)
                            ),
                            ChordFragment(text = " ", type = ChordFragmentType.CHORD_SPLITTER),
                            ChordFragment(
                                text = "C7/G",
                                type = ChordFragmentType.COMPOUND_CHORD,
                                compoundChord = CompoundChord(
                                    Chord(Note.C.index, false, "7"),
                                    "/",
                                    Chord(Note.G.index, false),
                                )
                            ),
                            ChordFragment(text = " ", type = ChordFragmentType.CHORD_SPLITTER),
                            ChordFragment(
                                text = "G",
                                type = ChordFragmentType.SINGLE_CHORD,
                                singleChord = Chord(Note.G.index, false)
                            ),
                        )
                    ),
                )
            )
        )
    }


    @Test
    fun test_formatWithKey() {
        val lyrics = LyricsParser().parseLyrics("""text [D/Ab G#m]""".trimIndent())
        ChordParser(ChordsNotation.ENGLISH).parseAndFillChords(lyrics)
        ChordsRenderer(ChordsNotation.ENGLISH, key = MajorKey.E_MAJOR).formatLyrics(lyrics)

        Assertions.assertThat(lyrics.lines[0]).isEqualTo(
            LyricsLine(
                listOf(
                    LyricsFragment(text = "text ", type = LyricsTextType.REGULAR_TEXT),
                    LyricsFragment(
                        text = "D/G# G#m", type = LyricsTextType.CHORDS, chordFragments = listOf(
                            ChordFragment(
                                text = "D/G#",
                                type = ChordFragmentType.COMPOUND_CHORD,
                                compoundChord = CompoundChord(
                                    Chord(Note.D.index, false),
                                    "/",
                                    Chord(
                                        Note.G_SHARP.index,
                                        false,
                                        "",
                                        originalModifier = NoteModifier.FLAT
                                    ),
                                )
                            ),
                            ChordFragment(text = " ", type = ChordFragmentType.CHORD_SPLITTER),
                            ChordFragment(
                                text = "G#m",
                                type = ChordFragmentType.SINGLE_CHORD,
                                singleChord = Chord(
                                    Note.G_SHARP.index,
                                    true,
                                    "",
                                    NoteModifier.SHARP
                                )
                            ),
                        )
                    ),
                )
            )
        )

        ChordsRenderer(ChordsNotation.ENGLISH, key = MajorKey.E_FLAT_MAJOR).formatLyrics(lyrics)
        Assertions.assertThat(lyrics.lines[0]).isEqualTo(
            LyricsLine(
                listOf(
                    LyricsFragment(text = "text ", type = LyricsTextType.REGULAR_TEXT),
                    LyricsFragment(
                        text = "D/Ab Abm", type = LyricsTextType.CHORDS, chordFragments = listOf(
                            ChordFragment(
                                text = "D/Ab",
                                type = ChordFragmentType.COMPOUND_CHORD,
                                compoundChord = CompoundChord(
                                    Chord(Note.D.index, false),
                                    "/",
                                    Chord(
                                        Note.A_FLAT.index,
                                        false,
                                        "",
                                        originalModifier = NoteModifier.FLAT
                                    ),
                                )
                            ),
                            ChordFragment(text = " ", type = ChordFragmentType.CHORD_SPLITTER),
                            ChordFragment(
                                text = "Abm",
                                type = ChordFragmentType.SINGLE_CHORD,
                                singleChord = Chord(
                                    Note.A_FLAT.index,
                                    true,
                                    "",
                                    originalModifier = NoteModifier.SHARP
                                )
                            ),
                        )
                    ),
                )
            )
        )
    }

}