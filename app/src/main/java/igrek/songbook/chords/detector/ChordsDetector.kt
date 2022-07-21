package igrek.songbook.chords.detector

import igrek.songbook.chords.syntax.*
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.util.lookup.SimpleCache

class ChordsDetector(notation: ChordsNotation? = null) {

    private val chordNameProvider = ChordNameProvider()

    private val falseFriends: Set<String> by lazy {
        when {
            notation != null -> chordNameProvider.falseFriends(notation)
            else -> emptySet()
        }
    }

    private val baseChordNames: Set<String> by lazy {
        val names = sortedSetOf(longestChordComparator)
        if (notation == null) {
            ChordsNotation.values().forEach { notation ->
                names.addAll(chordNameProvider.baseNotesNames(notation).flatten())
            }
        } else {
            names.addAll(chordNameProvider.baseNotesNames(notation).flatten())
        }
        names
    }

    private val minorChordNames: Set<String> by lazy {
        val names = sortedSetOf(longestChordComparator)
        if (notation == null) {
            ChordsNotation.values().forEach { notation ->
                names.addAll(chordNameProvider.minorChords(notation).flatten())
            }
        } else {
            names.addAll(chordNameProvider.minorChords(notation).flatten())
        }
        names
    }

    private val allBaseNames: Set<String> by lazy {
        minorChordNames union baseChordNames
    }

    private val baseChordToNoteIndex: Map<String, Int> by lazy {
        val allNames = hashMapOf<String, Int>()
        if (notation == null) {
            ChordsNotation.values().forEach { notation ->
                chordNameProvider.baseNotesNames(notation).forEachIndexed { index, names ->
                    names.forEach { name ->
                        allNames[name] = index
                    }
                }
            }
        } else {
            chordNameProvider.baseNotesNames(notation).forEachIndexed { index, names ->
                names.forEach { name ->
                    allNames[name] = index
                }
            }
        }
        allNames.toSortedMap(longestChordComparator)
    }

    private val minorChordToNoteIndex: SimpleCache<Map<String, Int>> = SimpleCache {
        val allNames = hashMapOf<String, Int>()
        if (notation == null) {
            ChordsNotation.values().forEach { notation ->
                chordNameProvider.minorChords(notation).forEachIndexed { index, names ->
                    names.forEach { name ->
                        allNames[name] = index
                    }
                }
            }
        } else {
            chordNameProvider.minorChords(notation).forEachIndexed { index, names ->
                names.forEach { name ->
                    allNames[name] = index
                }
            }
        }
        allNames.toSortedMap(longestChordComparator)
    }

    fun isWordAChord(word: String): Boolean {
        return isWordAChord(word, chordsPrimaryDelimiters)
                || isWordAChord(word, chordsAllDelimiters)
    }

    private fun isWordAChord(word: String, delimiters: Array<String>): Boolean {
        val splitted = word.split(*delimiters)

        // only delimiters word
        if (splitted.all { it.isEmpty() })
            return false

        splitted.forEach { fragment ->
            if (fragment.isNotEmpty()) {
                if (!isASingleChord(fragment))
                    return false
            }
        }

        return true
    }

    private fun isASingleChord(chordCandidate: String): Boolean {
        if (chordCandidate in falseFriends)
            return false

        return allBaseNames.any { chordBase ->
            if (chordCandidate == chordBase)
                return@any true

            if (chordCandidate.startsWith(chordBase)) {
                val remainder = chordCandidate.drop(chordBase.length)
                if (remainder in chordSuffixes)
                    return@any true
            }

            return@any false
        }
    }

    fun recognizeSingleChord(chord: String): Chord? {
        // recognize basic chord (without suffixes)
        minorChordToNoteIndex.get()[chord]?.let {
            return Chord(it, minor = true)
        }
        baseChordToNoteIndex[chord]?.let {
            return Chord(it, minor = false)
        }

        // recognize base chord + suffix
        minorChordToNoteIndex.get().forEach { (baseName: String, noteIndex: Int) ->
            if (chord.startsWith(baseName)) {
                val suffix = chord.drop(baseName.length)
                if (suffix in chordSuffixes)
                    return Chord(noteIndex, minor = true, suffix = suffix)
            }
        }
        baseChordToNoteIndex.forEach { (baseName: String, noteIndex: Int) ->
            if (chord.startsWith(baseName)) {
                val suffix = chord.drop(baseName.length)
                if (suffix in chordSuffixes)
                    return Chord(noteIndex, minor = false, suffix = suffix)
            }
        }

        return null
    }

}