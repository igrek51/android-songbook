package igrek.songbook.chords.parser

import igrek.songbook.chords.model.*
import igrek.songbook.settings.chordsnotation.ChordsNotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.test.assertNull


class ChordsParserTest {

    @Test
    fun test_recognize_major() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("C")).isEqualTo(Chord(0, false, ""))
        assertThat(parser.recognizeSingleChord("C#")).isEqualTo(Chord(1, false, "", NoteModifier.SHARP))
        assertThat(parser.recognizeSingleChord("D")).isEqualTo(Chord(2, false, ""))
        assertThat(parser.recognizeSingleChord("C#add9")).isEqualTo(Chord(1, false, "add9", NoteModifier.SHARP))

        assertThat(parser.recognizeSingleChord("Csus4")).isEqualTo(Chord(0, false, "sus4"))
        assertThat(parser.recognizeSingleChord("C#sus4")).isEqualTo(Chord(1, false, "sus4", NoteModifier.SHARP))
        assertThat(parser.recognizeSingleChord("Dsus4")).isEqualTo(Chord(2, false, "sus4"))

        assertNull(parser.recognizeSingleChord("dupa"))
    }

    @Test
    fun test_recognize_minor() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("c")).isEqualTo(Chord(0, true, ""))
        assertThat(parser.recognizeSingleChord("c#")).isEqualTo(Chord(1, true, "", NoteModifier.SHARP))
        assertThat(parser.recognizeSingleChord("d")).isEqualTo(Chord(2, true, ""))

        assertThat(parser.recognizeSingleChord("csus4")).isEqualTo(Chord(0, true, "sus4"))
        assertThat(parser.recognizeSingleChord("c#sus4")).isEqualTo(Chord(1, true, "sus4", NoteModifier.SHARP))
        assertThat(parser.recognizeSingleChord("dsus4")).isEqualTo(Chord(2, true, "sus4"))

        assertThat(parser.recognizeSingleChord("dis")).isEqualTo(Chord(3, true, "", NoteModifier.SHARP))
        assertThat(parser.recognizeSingleChord("es")).isEqualTo(Chord(3, true, "", NoteModifier.FLAT))

        assertNull(parser.recognizeSingleChord("dupa"))
    }

    @Test
    fun test_detect_fmaj7() {
        val parser = ChordParser(ChordsNotation.ENGLISH)
        assertThat(parser.recognizeSingleChord("Fmaj7")).isEqualTo(Chord(5, false, "maj7"))
        assertThat(parser.recognizeSingleChord("Fm")).isEqualTo(Chord(5, true, ""))
    }

    @Test
    fun test_c_plus() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("C+")).isEqualTo(Chord(0, false, "+"))
        assertThat(parser.recognizeSingleChord("C+")).isNotNull
    }

    @Test
    fun test_c_minus() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("C-")).isEqualTo(Chord(0, false, "-"))
        assertThat(parser.recognizeSingleChord("C-")).isNotNull
    }

    @Test
    fun test_detect_parentheses_Ch() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeCompoundChord("C")).isNull()
        assertThat(parser.recognizeSingleChord("C")).isEqualTo(Chord(0, false, ""))
        assertThat(parser.recognizeCompoundChord("C-h")).isNull()
        assertThat(parser.recognizeCompoundChord("(C-h)")).isNull()

        val unknowns = mutableSetOf<String>()
        val fragments = parser.parseChordFragments("Dmaj7(C-h)", unknowns)

        assertThat(unknowns).isEmpty()
        assertThat(fragments).hasSize(6)
        assertThat(fragments[0]).isEqualTo(ChordFragment("Dmaj7", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(2, false, "maj7")))
        assertThat(fragments[1]).isEqualTo(ChordFragment("(", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[2]).isEqualTo(ChordFragment("C", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(0, false, "")))
        assertThat(fragments[3]).isEqualTo(ChordFragment("-", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[4]).isEqualTo(ChordFragment("h", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(11, true, "")))
        assertThat(fragments[5]).isEqualTo(ChordFragment(")", ChordFragmentType.CHORD_SPLITTER))

    }

    @Test
    fun test_not_a_chord() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("Dupa")).isNull()
        assertThat(parser.recognizeSingleChord("Cmaj7blahblahblah")).isNull()
        assertThat(parser.recognizeSingleChord("Dsus888")).isNull()
        assertThat(parser.recognizeSingleChord(" ")).isNull()
        assertThat(parser.recognizeSingleChord("")).isNull()
        val parser2 = ChordParser(ChordsNotation.ENGLISH)
        assertThat(parser2.recognizeSingleChord("Dmupa")).isNull()
    }

    @Test
    fun test_suffix() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("Fmaj7")).isNotNull
        assertThat(parser.recognizeSingleChord("G#maj7-F")).isNull()
        assertThat(parser.recognizeCompoundChord("G#maj7-F")).isNull()
        assertThat(parser.recognizeCompoundChord("G#maj7/F")).isEqualTo(CompoundChord(
            Chord(8, false, "maj7", NoteModifier.SHARP), "/", Chord(5, false, "")
        ))
    }

    @Test
    fun test_minor_chords() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("cmaj7")).isNotNull
    }

    @Test
    fun test_cis() {
        val parser = ChordParser(ChordsNotation.GERMAN_IS)
        assertThat(parser.recognizeSingleChord("cismaj7")).isNotNull
    }

    @Test
    fun test_detect_english_minor() {
        val parser = ChordParser(ChordsNotation.ENGLISH)
        assertThat(parser.recognizeSingleChord("Cm")).isNotNull
        assertThat(parser.recognizeSingleChord("Dm")).isEqualTo(Chord(2, true, ""))
        assertThat(parser.recognizeSingleChord("Dmmaj7")).isEqualTo(Chord(2, true, "maj7"))
        assertThat(parser.recognizeSingleChord("Dmaj7")).isEqualTo(Chord(2, false, "maj7"))
        assertThat(parser.recognizeSingleChord("D")).isEqualTo(Chord(2, false, ""))
    }

    @Test
    fun test_slashed_chord() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeCompoundChord("C/H")).isNotNull
        assertThat(parser.recognizeCompoundChord("C/Y")).isNull()
        assertThat(parser.recognizeCompoundChord("a/G")).isNotNull
    }

    @Test
    fun test_g6add11() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("G6add11")).isNotNull
        assertThat(parser.recognizeSingleChord("G6add11")).isEqualTo(
            Chord(
                noteIndex = 7,
                minor = false,
                suffix = "6add11",
            )
        )
    }

    @Test
    fun test_false_friends() {
        val parser = ChordParser(ChordsNotation.GERMAN)
        assertThat(parser.recognizeSingleChord("Am")).isNull()
        assertThat(parser.recognizeSingleChord("co")).isNull()
    }

    @Test
    fun test_detect_solfege() {
        val parser = ChordParser(ChordsNotation.SOLFEGE)
        assertThat(parser.recognizeSingleChord("Do")).isEqualTo(Chord(0, false, ""))
        assertThat(parser.recognizeSingleChord("Dom")).isEqualTo(Chord(0, true, ""))
        assertThat(parser.recognizeSingleChord("DOm")).isEqualTo(Chord(0, true, ""))
        assertThat(parser.recognizeSingleChord("Do#m")).isEqualTo(Chord(1, true, "", originalModifier = NoteModifier.SHARP))
        assertThat(parser.recognizeSingleChord("Re")).isEqualTo(Chord(2, false, ""))
        assertThat(parser.recognizeSingleChord("Mibmaj7")).isEqualTo(Chord(3, false, "maj7", NoteModifier.FLAT))
    }

    @Test
    fun test_parseChordFragments() {
        val parser = ChordParser(ChordsNotation.ENGLISH)
        val unknowns = mutableSetOf<String>()

        val fragments = parser.parseChordFragments("Cmaj7/G# (D-D/E)   dupa", unknowns)

        assertThat(unknowns).contains("dupa")
        assertThat(fragments).hasSize(9)
        assertThat(fragments[0]).isEqualTo(ChordFragment("Cmaj7/G#", ChordFragmentType.COMPOUND_CHORD, compoundChord = CompoundChord(
            Chord(0, false, "maj7"), "/", Chord(8, false, "", NoteModifier.SHARP)
        )))
        assertThat(fragments[1]).isEqualTo(ChordFragment(" (", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[2]).isEqualTo(ChordFragment("D", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(2, false, "")))
        assertThat(fragments[3]).isEqualTo(ChordFragment("-", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[4]).isEqualTo(ChordFragment("D", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(2, false, "")))
        assertThat(fragments[5]).isEqualTo(ChordFragment("/", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[6]).isEqualTo(ChordFragment("E", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(4, false, "")))
        assertThat(fragments[7]).isEqualTo(ChordFragment(")   ", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[8]).isEqualTo(ChordFragment("dupa", ChordFragmentType.UNKNOWN_CHORD))
    }

    @Test
    fun test_parseAndFillChordsUnknowns() {
        val lyrics = LyricsExtractor().parseLyrics("""[a Cm dupa]""")
        val unkonwns = ChordParser(ChordsNotation.ENGLISH).parseAndFillChords(lyrics)
        assertThat(unkonwns).isEqualTo(setOf("a", "dupa"))
    }

    @Test
    fun test_parseGeneralChord() {
        val chord: GeneralChord? = ChordParser(ChordsNotation.ENGLISH).parseGeneralChord("Caug7")
        assertThat(chord?.baseChord?.noteIndex).isEqualTo(Note.C.index)
        assertThat(chord?.baseChord?.suffix).isEqualTo("aug7")
    }

    @Test
    fun test_parseDashedChords() {
        val input = "[Dmaj7-D]"
        val lyrics = LyricsExtractor().parseLyrics(input)
        ChordParser(ChordsNotation.ENGLISH).parseAndFillChords(lyrics)
        val fragments = lyrics.lines
            .flatMap { line -> line.fragments }
            .filter { it.type == LyricsTextType.CHORDS }
            .flatMap { fragment -> fragment.chordFragments }

        assertThat(fragments).hasSize(3)
        assertThat(fragments[0]).isEqualTo(ChordFragment("Dmaj7", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(2, false, "maj7")))
        assertThat(fragments[1]).isEqualTo(ChordFragment("-", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[2]).isEqualTo(ChordFragment("D", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(2, false, "")))
    }

    @Test
    fun test_parseDashedChords2() {
        val input = "[Dmaj7-D D-Dsus4-Dsus2]"
        val lyrics = LyricsExtractor().parseLyrics(input)
        ChordParser(ChordsNotation.ENGLISH).parseAndFillChords(lyrics)
        val fragments = lyrics.lines
            .flatMap { line -> line.fragments }
            .filter { it.type == LyricsTextType.CHORDS }
            .flatMap { fragment -> fragment.chordFragments }

        assertThat(fragments).hasSize(9)
        assertThat(fragments[0]).isEqualTo(ChordFragment("Dmaj7", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(2, false, "maj7")))
        assertThat(fragments[1]).isEqualTo(ChordFragment("-", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[2]).isEqualTo(ChordFragment("D", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(2, false, "")))
        assertThat(fragments[3]).isEqualTo(ChordFragment(" ", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[4]).isEqualTo(ChordFragment("D", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(2, false, "")))
        assertThat(fragments[5]).isEqualTo(ChordFragment("-", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[6]).isEqualTo(ChordFragment("Dsus4", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(2, false, "sus4")))
        assertThat(fragments[7]).isEqualTo(ChordFragment("-", ChordFragmentType.CHORD_SPLITTER))
        assertThat(fragments[8]).isEqualTo(ChordFragment("Dsus2", ChordFragmentType.SINGLE_CHORD, singleChord = Chord(2, false, "sus2")))
    }
}
