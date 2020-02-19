package igrek.songbook.chords.detector

import igrek.songbook.chords.ChordsConverter
import igrek.songbook.chords.diagram.allGuitarChordsDiagrams
import igrek.songbook.chords.diagram.allMandolinChordsDiagrams
import igrek.songbook.chords.diagram.allUkuleleChordsDiagrams
import igrek.songbook.chords.syntax.chordsPrimaryDelimiters
import igrek.songbook.chords.syntax.singleChordsDelimiters
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.instrument.ChordsInstrument
import igrek.songbook.songpreview.lyrics.LyricsModel
import igrek.songbook.songpreview.lyrics.LyricsTextType

class UniqueChordsFinder(
        instrument: ChordsInstrument
) {

    private var toEnglishConverter = ChordsConverter(ChordsNotation.GERMAN, ChordsNotation.ENGLISH)
    private val chordDiagramCodes = getChordDiagrams(instrument)

    fun findUniqueChordsInLyrics(crdModel: LyricsModel): Set<String> {
        val chordFragments = crdModel.lines
                .flatMap { line -> line.fragments }
                .filter { it.type == LyricsTextType.CHORDS }
                .map { fragment -> fragment.text }
        return findUniqueChords(chordFragments)
    }

    fun findUniqueChords(chordFragments: List<String>): Set<String> {
        val complexChords = chordFragments.flatMap { fragment ->
            fragment.split(*chordsPrimaryDelimiters)
        }

        // split by primary delimiters first
        return complexChords.map { complexChord ->
            // convert complex chord
            val (engComplexChord, unrecognized) = toEnglishConverter.convertChordsGroup(complexChord)
            if (unrecognized.isNotEmpty()) {
                logger.warn("Unrecognized chords: $unrecognized")
                return@map emptyList<String>()
            }
            // try to match diagram as a whole
            if (engComplexChord in chordDiagramCodes) {
                return@map listOf(complexChord)
            }

            // if not recognized, split further
            val subchords = complexChord.split(*singleChordsDelimiters)
            return@map subchords.filter { subchord ->
                subchord in chordDiagramCodes
            }
        }.flatten().distinct().toSet()
    }

    private fun getChordDiagrams(instrument: ChordsInstrument): Map<String, List<String>> {
        return when (instrument) {
            ChordsInstrument.GUITAR -> allGuitarChordsDiagrams.get()
            ChordsInstrument.UKULELE -> allUkuleleChordsDiagrams.get()
            ChordsInstrument.MANDOLIN -> allMandolinChordsDiagrams.get()
        }
    }
}