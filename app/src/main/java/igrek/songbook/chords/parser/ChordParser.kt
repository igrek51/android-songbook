package igrek.songbook.chords.parser

import igrek.songbook.chords.model.Chord
import igrek.songbook.chords.model.ChordFragment
import igrek.songbook.chords.model.ChordFragmentType
import igrek.songbook.chords.model.CompoundChord
import igrek.songbook.chords.model.GeneralChord
import igrek.songbook.chords.model.LyricsModel
import igrek.songbook.chords.model.LyricsTextType
import igrek.songbook.chords.model.NoteModifier
import igrek.songbook.chords.syntax.ChordNames
import igrek.songbook.chords.syntax.chordSuffixes
import igrek.songbook.chords.syntax.compoundChordAllowedSplitters
import igrek.songbook.chords.syntax.compoundChordsDelimiters
import igrek.songbook.chords.syntax.regexSplitSingleChordsWithDelimiters
import igrek.songbook.chords.syntax.regexSplitcompoundChordsWithDelimiters
import igrek.songbook.chords.syntax.singleChordsDelimiters
import igrek.songbook.settings.chordsnotation.ChordsNotation


class ChordParser(
    private val notation: ChordsNotation,
) {

    fun recognizeSingleChord(chord: String): Chord? {
        if (chord in (ChordNames.falseFriends[notation] ?: emptySet()))
            return null

        // recognize basic chord (without suffixes)
        minorChordToNoteIndex[chord]?.let { noteIndex: Int ->
            return Chord(
                noteIndex = noteIndex,
                minor = true,
                originalModifier = getNoteModifier(chord),
            )
        }
        baseChordToNoteIndex[chord]?.let { noteIndex: Int ->
            return Chord(
                noteIndex = noteIndex,
                minor = false,
                originalModifier = getNoteModifier(chord),
            )
        }

        // recognize base chord + suffix
        minorChordToNoteIndex.forEach { (baseName: String, noteIndex: Int) ->
            if (chord.startsWith(baseName)) {
                val suffix = chord.drop(baseName.length)
                if (suffix in chordSuffixes)
                    return Chord(
                        noteIndex = noteIndex,
                        minor = true,
                        suffix = suffix,
                        originalModifier = getNoteModifier(baseName),
                    )
            }
        }
        baseChordToNoteIndex.forEach { (baseName: String, noteIndex: Int) ->
            if (chord.startsWith(baseName)) {
                val suffix = chord.drop(baseName.length)
                if (suffix in chordSuffixes)
                    return Chord(
                        noteIndex = noteIndex,
                        minor = false,
                        suffix = suffix,
                        originalModifier = getNoteModifier(baseName),
                    )
            }
        }
        return null
    }

    fun recognizeCompoundChord(
        chord: String,
    ): CompoundChord? {
        val fragments = chord.split(regexSplitSingleChordsWithDelimiters)
            .filter { it.isNotEmpty() }
            .map { part ->
                if (part in singleChordsDelimiters) {
                    return@map ChordFragment(
                        text = part,
                        type = ChordFragmentType.CHORD_SPLITTER,
                    )
                }

                val singleChord = recognizeSingleChord(part) ?: return null // propagate failure
                return@map ChordFragment(
                    text = part,
                    type = ChordFragmentType.SINGLE_CHORD,
                    singleChord = singleChord,
                )
            }

        if (fragments.isEmpty())
            return null

        if (fragments.all { it.type == ChordFragmentType.CHORD_SPLITTER })
            return null

        if (fragments.size != 3)
            return null

        val splitter = fragments[1]
        val former = fragments[0]
        val latter = fragments[2]

        if (!(
            splitter.type == ChordFragmentType.CHORD_SPLITTER
            && former.type == ChordFragmentType.SINGLE_CHORD
            && latter.type == ChordFragmentType.SINGLE_CHORD
            && splitter.text in compoundChordAllowedSplitters
        ))
            return null

        return CompoundChord(
            chord1 = former.singleChord!!,
            splitter = splitter.text,
            chord2 = latter.singleChord!!,
        )
    }

    fun parseAndFillChords(lyrics: LyricsModel): Set<String> {
        val unknowns = mutableSetOf<String>()

        lyrics.lines.forEach { line ->
            line.fragments.forEach { lyricsFragment ->
                if (lyricsFragment.type == LyricsTextType.CHORDS) {
                    lyricsFragment.chordFragments =
                        parseChordFragments(lyricsFragment.text, unknowns)
                }
            }
        }

        return unknowns
    }

    fun parseChordFragments(
        text: String,
        unknowns: MutableSet<String>,
    ): List<ChordFragment> {
        val fragments: List<ChordFragment> = text.split(regexSplitcompoundChordsWithDelimiters)
            .filter { it.isNotEmpty() }
            .flatMap { part ->

                if (part in compoundChordsDelimiters) {
                    return@flatMap listOf(
                        ChordFragment(
                            text = part,
                            type = ChordFragmentType.CHORD_SPLITTER,
                        )
                    )
                }

                recognizeCompoundChord(part)?.let { compoundChord ->
                    return@flatMap listOf(
                        ChordFragment(
                            text = part,
                            type = ChordFragmentType.COMPOUND_CHORD,
                            compoundChord = compoundChord,
                        )
                    )
                }

                // split further, breaking into single chords
                return@flatMap parseSingleChordFragments(part, unknowns)
            }

        // join adjacent splitters
        return fragments.fold(mutableListOf()) { acc: MutableList<ChordFragment>, segment: ChordFragment ->
            if (acc.isNotEmpty() &&
                acc.last().type == ChordFragmentType.CHORD_SPLITTER &&
                segment.type == ChordFragmentType.CHORD_SPLITTER
            ) {
                acc.last().text += segment.text // join with the last group
            } else {
                acc += segment // add new group
            }
            acc
        }
    }

    fun parseSingleChordFragments(
        text: String,
        unknowns: MutableSet<String>,
    ): List<ChordFragment> {
        return text.split(regexSplitSingleChordsWithDelimiters)
            .filter { it.isNotEmpty() }
            .map { singlePart ->
                if (singlePart in singleChordsDelimiters) {
                    return@map ChordFragment(
                        text = singlePart,
                        type = ChordFragmentType.CHORD_SPLITTER,
                    )
                }

                val singleChord = recognizeSingleChord(singlePart)
                if (singleChord != null) {
                    return@map ChordFragment(
                        text = singlePart,
                        type = ChordFragmentType.SINGLE_CHORD,
                        singleChord = singleChord,
                    )
                } else {
                    unknowns.add(singlePart)
                    return@map ChordFragment(
                        text = singlePart,
                        type = ChordFragmentType.UNKNOWN_CHORD,
                    )
                }
            }
    }

    fun isWordAChord(word: String): Boolean {
        // is word or sentence a chord or a group of chords
        val unknowns = mutableSetOf<String>()
        val chordFragments = parseChordFragments(word, unknowns)
        if (chordFragments.any { it.type == ChordFragmentType.UNKNOWN_CHORD })
            return false
        return chordFragments.any {
            it.type == ChordFragmentType.SINGLE_CHORD || it.type == ChordFragmentType.COMPOUND_CHORD
        }
    }

    fun parseGeneralChord(chord: String): GeneralChord? {
        val fragments = chord.split(regexSplitSingleChordsWithDelimiters)
            .filter { it.isNotEmpty() }
            .map { part ->
                if (part in singleChordsDelimiters) {
                    return@map ChordFragment(
                        text = part,
                        type = ChordFragmentType.CHORD_SPLITTER,
                    )
                }

                val singleChord: Chord =
                    recognizeSingleChord(part) ?: return null // propagate failure
                return@map ChordFragment(
                    text = part,
                    type = ChordFragmentType.SINGLE_CHORD,
                    singleChord = singleChord,
                )
            }

        if (fragments.isEmpty())
            return null

        if (fragments.all { it.type == ChordFragmentType.CHORD_SPLITTER })
            return null

        if (fragments.size == 3 &&
            fragments[0].type == ChordFragmentType.SINGLE_CHORD &&
            fragments[1].type == ChordFragmentType.CHORD_SPLITTER &&
            fragments[2].type == ChordFragmentType.SINGLE_CHORD
        ) {
            return CompoundChord(
                chord1 = fragments[0].singleChord!!,
                splitter = fragments[1].text,
                chord2 = fragments[2].singleChord!!,
            )
        }

        if (fragments.size == 1) {
            val fragment = fragments[0]
            if (fragment.type == ChordFragmentType.SINGLE_CHORD) {
                return fragment.singleChord
            }
        }
        return null
    }

    private val baseChordToNoteIndex: Map<String, Int> by lazy {
        val allNames = hashMapOf<String, Int>()
        ChordNames.validMajorChordNames[notation]!!.forEachIndexed { index, names ->
            names.forEach { name ->
                allNames[name] = index
            }
        }
        allNames.toSortedMap(longestChordComparator)
    }

    private val minorChordToNoteIndex: Map<String, Int> by lazy {
        val allNames = hashMapOf<String, Int>()
        ChordNames.validMinorChordNames[notation]!!.forEachIndexed { index, names ->
            names.forEach { name ->
                allNames[name] = index
            }
        }
        allNames.toSortedMap(longestChordComparator)
    }

    private fun getNoteModifier(note: String): NoteModifier {
        return when (note) {
            in ChordNames.sharpNotes[notation]!! -> NoteModifier.SHARP
            in ChordNames.flatNotes[notation]!! -> NoteModifier.FLAT
            else -> NoteModifier.NATURAL
        }
    }

    private val longestChordComparator = Comparator { lhs: String, rhs: String ->
        if (rhs.length != lhs.length) {
            rhs.length - lhs.length
        } else {
            lhs.compareTo(rhs)
        }
    }

}
