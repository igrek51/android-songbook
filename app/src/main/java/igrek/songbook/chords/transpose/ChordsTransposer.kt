package igrek.songbook.chords.transpose

import igrek.songbook.chords.model.*

class ChordsTransposer {

    fun transposeLyrics(lyrics: LyricsModel, transposition: Int): LyricsModel {
        val lyricsClone = LyricsCloner().cloneLyrics(lyrics)
        if (transposition == 0)
            return lyricsClone
        lyricsClone.lines.forEach { line ->
            line.fragments.forEach { lyricsFragment ->
                lyricsFragment.chordFragments.forEach { chordFragment: ChordFragment ->
                    transposeChordFragment(chordFragment, transposition)
                }
            }
        }
        return lyricsClone
    }

    private fun transposeChordFragment(chordFragment: ChordFragment, transposition: Int) {
        when (chordFragment.type) {
            ChordFragmentType.SINGLE_CHORD -> {
                transposeSingleChord(chordFragment.singleChord!!, transposition)
            }
            ChordFragmentType.COMPOUND_CHORD -> {
                transposeSingleChord(chordFragment.compoundChord!!.chord1, transposition)
                transposeSingleChord(chordFragment.compoundChord.chord2, transposition)
            }
            else -> {}
        }
    }

    private fun transposeSingleChord(chord: Chord, transposition: Int) {
        chord.noteIndex = transposeNote(chord.noteIndex, transposition)
    }

    private fun transposeNote(noteIndex: Int, transposition: Int): Int {
        return (noteIndex + transposition + 12) % 12
    }
}
