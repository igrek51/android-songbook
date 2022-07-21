package igrek.songbook.chordsv2.model

class LyricsCloner {

    fun cloneLyrics(origin: LyricsModel): LyricsModel {
        return origin.copy(
            lines = origin.lines.map { cloneLine(it) },
        )
    }

    fun cloneLine(origin: LyricsLine): LyricsLine {
        return origin.copy(
            fragments = origin.fragments.map { cloneLyricsFragment(it) },
        )
    }

    fun cloneLyricsFragment(origin: LyricsFragment): LyricsFragment {
        return origin.copy(
            chordFragments = origin.chordFragments.map { cloneChordFragment(it) },
        )
    }

    fun cloneChordFragment(origin: ChordFragment): ChordFragment {
        return when (origin.type) {
            ChordFragmentType.SINGLE_CHORD -> origin.copy(singleChord = origin.singleChord?.copy())
            ChordFragmentType.COMPOUND_CHORD -> origin.copy(compoundChord = origin.compoundChord?.clone())
            ChordFragmentType.CHORD_SPLITTER -> origin.copy()
            ChordFragmentType.UNKNOWN_CHORD -> origin.copy()
        }
    }

}