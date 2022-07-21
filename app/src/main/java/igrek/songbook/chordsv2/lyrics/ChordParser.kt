package igrek.songbook.chordsv2.lyrics

import igrek.songbook.chordsv2.model.*
import igrek.songbook.chordsv2.syntax.*
import igrek.songbook.settings.chordsnotation.ChordsNotation


class ChordParser(
    private val notation: ChordsNotation,
) {

    fun parseChords(lyrics: LyricsModel): LyricsModel {
        val unknowns = mutableSetOf<String>()

        lyrics.lines.forEach { line ->
            line.fragments.forEach { fragment ->
                if (fragment.type == LyricsTextType.CHORDS) {
                    parseAndFillChordFragment(fragment, unknowns)
                }
            }
        }

        return lyrics
    }

    private fun parseAndFillChordFragment(
        lyricsFragment: LyricsFragment,
        unknowns: MutableSet<String>,
    ) {
        val compoundChord = recognizeCompoundChord(lyricsFragment.text, unknowns)
        lyricsFragment.chordFragments = compoundChord?.chordFragments ?: emptyList()
    }

    fun recognizeSingleChord(chord: String): Chord? {
        if (chord in ChordNames.falseFriends[notation]!!)
            return null

        // recognize basic chord (without suffixes)
        minorChordToNoteIndex[chord]?.let { noteIndex: Int ->
            return Chord(
                displayText=chord,
                noteIndex=noteIndex,
                minor=true,
                suffix="",
                originalNoteIndex=noteIndex,
                originalText=chord,
                originalNotation=notation,
            )
        }
        baseChordToNoteIndex[chord]?.let { noteIndex: Int ->
            return Chord(
                displayText=chord,
                noteIndex=noteIndex,
                minor=false,
                suffix="",
                originalNoteIndex=noteIndex,
                originalText=chord,
                originalNotation=notation,
            )
        }

        // recognize base chord + suffix
        minorChordToNoteIndex.forEach { (baseName: String, noteIndex: Int) ->
            if (chord.startsWith(baseName)) {
                val suffix = chord.drop(baseName.length)
                if (suffix in chordSuffixes)
                    return Chord(
                        displayText=chord,
                        noteIndex=noteIndex,
                        minor=true,
                        suffix=suffix,
                        originalNoteIndex=noteIndex,
                        originalText=chord,
                        originalNotation=notation,
                    )
            }
        }
        baseChordToNoteIndex.forEach { (baseName: String, noteIndex: Int) ->
            if (chord.startsWith(baseName)) {
                val suffix = chord.drop(baseName.length)
                if (suffix in chordSuffixes)
                    return Chord(
                        displayText=chord,
                        noteIndex=noteIndex,
                        minor=false,
                        suffix=suffix,
                        originalNoteIndex=noteIndex,
                        originalText=chord,
                        originalNotation=notation,
                    )
            }
        }

        return null
    }

    fun recognizeCompoundChord(
        chord: String,
        unknowns: MutableSet<String> = mutableSetOf(),
    ): CompoundChord? {
        val splitted = chord.split(regexSplitSingleChordsWithDelimiters)

        val fragments = splitted.mapNotNull { part ->
            if (part.isEmpty()) {
                return@mapNotNull null
            }

            if (part in singleChordsDelimiters) {
                return@mapNotNull ChordFragment(
                    text = part,
                    type = ChordFragmentType.CHORD_SPLITTER,
                )
            }

            val singleChord = recognizeSingleChord(part)
            if (singleChord == null) {
                unknowns.add(part)
                return null
            }

            return@mapNotNull ChordFragment(
                text = part,
                type = ChordFragmentType.CHORD,
                chord = singleChord,
            )
        }

        if (fragments.isEmpty()) {
            unknowns.add(chord)
            return null
        }

        if (fragments.all { it.type == ChordFragmentType.CHORD_SPLITTER }) {
            unknowns.add(chord)
            return null
        }

        return CompoundChord(text=chord, chordFragments=fragments)
    }

    private val baseChordToNoteIndex: Map<String, Int> by lazy {
        val allNames = hashMapOf<String, Int>()
        ChordNames.baseNoteNames[notation]!!.forEachIndexed { index, names ->
            names.forEach { name ->
                allNames[name] = index
            }
        }
        allNames.toSortedMap(longestChordComparator)
    }

    private val minorChordToNoteIndex: Map<String, Int> by lazy {
        val allNames = hashMapOf<String, Int>()
        ChordNames.minorChordNames[notation]!!.forEachIndexed { index, names ->
            names.forEach { name ->
                allNames[name] = index
            }
        }
        allNames.toSortedMap(longestChordComparator)
    }

}
