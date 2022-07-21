package igrek.songbook.chordsv2

import igrek.songbook.chordsv2.model.*
import igrek.songbook.settings.chordsnotation.ChordsNotation

class ChordsFormatter (
    private val notation: ChordsNotation,
    private val keyModifier: NoteModifier? = null,
) {

    fun formatLyrics(lyrics: LyricsModel): LyricsModel {
        lyrics.lines.forEach { line ->
            line.fragments.forEach(::renderLyricsFragment)
        }
        return lyrics
    }

    private fun renderLyricsFragment(lyricsFragment: LyricsFragment) {
        if (lyricsFragment.type == LyricsTextType.CHORDS) {
            lyricsFragment.chordFragments.forEach { chordFragment: ChordFragment ->
                renderChordFragment(chordFragment)
            }
            lyricsFragment.text = lyricsFragment.chordFragments.joinToString("")
        }
    }

    private fun renderChordFragment(chordFragment: ChordFragment) {
        when (chordFragment.type) {
            ChordFragmentType.SINGLE_CHORD -> {
                chordFragment.text = chordFragment.singleChord!!.format(notation, keyModifier)
            }
            ChordFragmentType.COMPOUND_CHORD -> {
                chordFragment.text = chordFragment.compoundChord!!.format(notation, keyModifier)
            }
            else -> {}
        }
    }
}