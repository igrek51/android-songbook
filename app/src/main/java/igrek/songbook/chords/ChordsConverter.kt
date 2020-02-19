package igrek.songbook.chords

import igrek.songbook.chords.detector.Chord
import igrek.songbook.chords.detector.ChordsDetector
import igrek.songbook.chords.syntax.ChordNameProvider
import igrek.songbook.chords.syntax.chordsGroupRegex
import igrek.songbook.chords.syntax.singleChordsSplitRegex
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
            val chordsGroup = matchResult.groupValues[1]
            val (converted, unrecognized) = convertChordsGroup(chordsGroup)
            if (unrecognized.isNotEmpty()) {
                logger.warn("Unrecognized chords: $unrecognized")
            }
            "[$converted]"
        }
    }

    fun convertChordsGroup(chordsGroup: String): Pair<String, List<String>> {
        val unrecognized = mutableListOf<String>()
        val converted = "$chordsGroup ".replace(singleChordsSplitRegex) { matchResult ->
            val singleChord = matchResult.groupValues[1]
            val separator = matchResult.groupValues[2]
            var converted = convertSingleChord(singleChord)
            if (converted == null) {
                unrecognized.add(singleChord)
                converted = singleChord
            }
            converted + separator
        }.dropLast(1)
        return converted to unrecognized
    }

    fun convertSingleChord(chord: String): String? {
        if (chord.trim { it <= ' ' }.isEmpty())
            return chord

        val recognized: Chord = fromChordsDetector.recognizeSingleChord(chord)
                ?: return null

        val prefix = when (recognized.minor) {
            false -> noteIndexToBaseName[recognized.noteIndex]
            true -> noteIndexToMinorName[recognized.noteIndex]
        }

        return prefix + recognized.suffix
    }

}