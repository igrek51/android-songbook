package igrek.songbook.chords.transpose

import igrek.songbook.chords.detector.Chord
import igrek.songbook.chords.detector.ChordsDetector
import igrek.songbook.chords.syntax.ChordNameProvider
import igrek.songbook.chords.syntax.chordsGroupRegex
import igrek.songbook.chords.syntax.singleChordsSplitRegex
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.settings.chordsnotation.ChordsNotation

class ChordsTransposer(
        fromNotation: ChordsNotation,
        private val toNotation: ChordsNotation,
) {

    private val chordNameProvider = ChordNameProvider()
    private val chordsDetector = ChordsDetector(fromNotation)

    private val noteIndexToBaseName: Map<Int, String> by lazy {
        val map = hashMapOf<Int, String>()
        chordNameProvider.baseNotesNames(toNotation).forEachIndexed { index, names ->
            if (names.isNotEmpty())
                map[index] = names.first()
        }
        map
    }

    private val noteIndexToLowerName: Map<Int, String> by lazy {
        val map = hashMapOf<Int, String>()
        chordNameProvider.minorChords(toNotation).forEachIndexed { index, names ->
            if (names.isNotEmpty())
                map[index] = names.first()
        }
        map
    }

    fun transposeLyrics(lyrics: String, t: Int): String {
        return lyrics.replace(chordsGroupRegex) { matchResult ->
            val chordsGroup = matchResult.groupValues[1] + " "
            val replaced = chordsGroup.replace(singleChordsSplitRegex) { matchResult2 ->
                val singleChord = matchResult2.groupValues[1]
                val separator = matchResult2.groupValues[2]
                transposeChord(singleChord, t) + separator
            }.dropLast(1)
            "[$replaced]"
        }
    }

    /**
     * @param chord chord in format: C, C#, c, c#, Cmaj7, c7, c#7, Cadd9, Csus
     * @param t     shift semitones count
     * @return chord transposed by semitones
     */
    fun transposeChord(chord: String, t: Int): String {
        if (chord.trim { it <= ' ' }.isEmpty())
            return chord

        val recognized: Chord? = chordsDetector.recognizeSingleChord(chord)
        if (recognized == null) {
            logger.warn("Chords detector: chord not recognized: \"$chord\"")
            return chord
        }

        val transposedNote = (recognized.noteIndex + t + 12) % 12

        val prefix = when(recognized.minor) {
            false -> noteIndexToBaseName[transposedNote]
            true -> noteIndexToLowerName[transposedNote]
        }

        return prefix + recognized.suffix
    }

}
