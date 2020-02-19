package igrek.songbook.chords

import igrek.songbook.chords.detector.Chord
import igrek.songbook.chords.detector.ChordsDetector
import igrek.songbook.chords.syntax.ChordNameProvider
import igrek.songbook.chords.syntax.chordsGroupRegex
import igrek.songbook.chords.syntax.chordsSplitRegex
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.settings.chordsnotation.ChordsNotation

class ChordsConverter(
        fromNotation: ChordsNotation,
        toNotation: ChordsNotation
) {

    private val chordNameProvider = ChordNameProvider()
    private val fromChordsDetector = ChordsDetector(fromNotation)

    private val noteIndexToBaseName: Map<Int, String> by lazy {
        val map = hashMapOf<Int, String>()
        chordNameProvider.baseNotesNames(toNotation).forEachIndexed { index, names ->
            if (names.isNotEmpty())
                map[index] = names.first()
        }
        map
    }

    private val noteIndexToMinorName: Map<Int, String> by lazy {
        val map = hashMapOf<Int, String>()
        chordNameProvider.minorChords(toNotation).forEachIndexed { index, names ->
            if (names.isNotEmpty())
                map[index] = names.first()
        }
        map
    }

    fun convertLyrics(lyrics: String): String {
        return lyrics.replace(chordsGroupRegex) { matchResult ->
            val chordsGroup = matchResult.groupValues[1] + " "
            val replaced = chordsGroup.replace(chordsSplitRegex) { matchResult2 ->
                val singleChord = matchResult2.groupValues[1]
                val separator = matchResult2.groupValues[2]
                convertChord(singleChord) + separator
            }.dropLast(1)
            "[$replaced]"
        }
    }

    fun convertChord(chord: String): String {
        if (chord.trim { it <= ' ' }.isEmpty())
            return chord

        val recognized: Chord? = fromChordsDetector.recognizeSingleChord(chord)
        if (recognized == null) {
            logger.warn("Chords detector: chord not recognized: \"$chord\"")
            return chord
        }

        val prefix = when(recognized.minor) {
            false -> noteIndexToBaseName[recognized.noteIndex]
            true -> noteIndexToMinorName[recognized.noteIndex]
        }

        return prefix + recognized.suffix
    }

}