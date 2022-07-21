package igrek.songbook.chords.detect

import igrek.songbook.chords.model.*

class UniqueChordsFinder {

    fun findUniqueSingleChordsInLyrics(lyrics: LyricsModel): Set<Chord> {
        val chordFragments = lyrics.lines
            .flatMap { line -> line.fragments }
            .filter { it.type == LyricsTextType.CHORDS }
            .flatMap { fragment -> fragment.chordFragments }
        val singleChords = chordFragments
            .filter { it.type == ChordFragmentType.SINGLE_CHORD }
            .mapNotNull { it.singleChord }
        val compoundChords = chordFragments
            .filter { it.type == ChordFragmentType.COMPOUND_CHORD }
            .mapNotNull { it.compoundChord }

        val allSingleChords = singleChords + compoundChords.map { it.chord1 }
        return allSingleChords.distinct().toSet()
    }

    fun findUniqueNotesInLyrics(lyrics: LyricsModel): Set<Int> {
        val chordFragments = lyrics.lines
            .flatMap { line -> line.fragments }
            .filter { it.type == LyricsTextType.CHORDS }
            .flatMap { fragment -> fragment.chordFragments }
        val singleChords = chordFragments
            .filter { it.type == ChordFragmentType.SINGLE_CHORD }
            .mapNotNull { it.singleChord }
        val compoundChords = chordFragments
            .filter { it.type == ChordFragmentType.COMPOUND_CHORD }
            .mapNotNull { it.compoundChord }

        val allSingleChords = singleChords + compoundChords.map { it.chord1 }
        return findUniqueNotes(allSingleChords)
    }

    fun findUniqueChordNamesInLyrics(lyrics: LyricsModel): Set<String> {
        val chordFragments = lyrics.lines
            .flatMap { line -> line.fragments }
            .filter { it.type == LyricsTextType.CHORDS }
            .flatMap { fragment -> fragment.chordFragments }
        val singleChords = chordFragments
            .filter { it.type == ChordFragmentType.SINGLE_CHORD }
            .map { it.text }
        val compoundChords = chordFragments
            .filter { it.type == ChordFragmentType.COMPOUND_CHORD }
            .map { it.text }

        return (singleChords + compoundChords).toSet()
    }

    private fun findUniqueNotes(chords: List<Chord>): Set<Int> {
        return chords
            .map { it.noteIndex }
            .distinct()
            .toSet()
    }
}