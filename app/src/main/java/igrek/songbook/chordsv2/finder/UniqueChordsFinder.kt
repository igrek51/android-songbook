package igrek.songbook.chordsv2.finder

import igrek.songbook.chordsv2.model.*

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

    fun findUniqueNotesInLyrics(lyrics: LyricsModel): Set<Note> {
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

    private fun findUniqueNotes(chords: List<Chord>): Set<Note> {
        return chords
            .map { indexToNote(it.noteIndex, it.noteModifier) }
            .distinct()
            .toSet()
    }
}