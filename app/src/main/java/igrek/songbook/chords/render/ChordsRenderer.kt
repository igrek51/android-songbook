package igrek.songbook.chords.render

import igrek.songbook.chords.model.*
import igrek.songbook.chords.syntax.MajorKey
import igrek.songbook.settings.chordsnotation.ChordsNotation

class ChordsRenderer (
    private val toNotation: ChordsNotation,
    private val key: MajorKey? = null,
    private val forceSharps: Boolean = false,
) {

    fun formatLyrics(lyrics: LyricsModel, originalModifiers: Boolean = false): LyricsModel {
        lyrics.lines.forEach { line ->
            line.fragments.forEach { renderLyricsFragment(it, originalModifiers) }
        }
        return lyrics
    }

    fun renderLyricsFragment(lyricsFragment: LyricsFragment, originalModifiers: Boolean = false) {
        if (lyricsFragment.type == LyricsTextType.CHORDS) {
            lyricsFragment.chordFragments.forEach { chordFragment: ChordFragment ->
                renderChordFragment(chordFragment, originalModifiers)
            }
            lyricsFragment.text = lyricsFragment.chordFragments.joinToString("") { it.text }
        }
    }

    private fun renderChordFragment(chordFragment: ChordFragment, originalModifiers: Boolean) {
        when (chordFragment.type) {
            ChordFragmentType.SINGLE_CHORD -> {
                chordFragment.text = chordFragment.singleChord!!.format(toNotation, key, originalModifiers, forceSharps)
            }
            ChordFragmentType.COMPOUND_CHORD -> {
                chordFragment.text = chordFragment.compoundChord!!.format(toNotation, key, originalModifiers, forceSharps)
            }
            else -> {}
        }
    }
}