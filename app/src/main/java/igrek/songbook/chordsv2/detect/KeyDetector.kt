package igrek.songbook.chordsv2.finder

import igrek.songbook.chordsv2.model.*
import igrek.songbook.chordsv2.syntax.MajorKey
import kotlin.math.absoluteValue

class KeyDetector {

    fun detectKey(lyrics: LyricsModel): MajorKey {
        val scores = detectKeyScores(lyrics)
        val bestMatch = scores.maxByOrNull { it.value }!!
        return bestMatch.key
    }

    fun detectKeyScores(lyrics: LyricsModel): Map<MajorKey, Int> {
        val uniqueChords = UniqueChordsFinder().findUniqueSingleChordsInLyrics(lyrics)
        val uniqueNotes = uniqueChords.map { it.noteIndex }.distinct().toSet()
        val majorChordNotes = uniqueChords.filter { !it.minor }.map { it.noteIndex }.toSet()
        val minorChordNotes = uniqueChords.filter { it.minor }.map { it.noteIndex }.toSet()

        return MajorKey.values().associateWith { key ->
            assessKeyMatchScore(key, uniqueNotes, majorChordNotes, minorChordNotes)
        }
    }

    private fun assessKeyMatchScore(key: MajorKey, uniqueNotes: Set<Int>, majorChordNotes: Set<Int>, minorChordNotes: Set<Int>): Int {

        val notesIntersection = majorKeyNoteIndexes[key]!!.intersect(uniqueNotes)
        val notesIntersectionScore = (2 * notesIntersection.size - uniqueNotes.size) * 100

        val hasMajorChord = key.baseMajorNote.index in majorChordNotes
        val hasMinorChord = key.baseMinorNote.index in minorChordNotes
        val baseChordsScore = hasMajorChord.toInt() * 10 + hasMinorChord.toInt() * 10

        val sharpnessAbs = majorKeySharpnessAbs[key]!!
        val sharpnessScore = -sharpnessAbs // take key with less sharps/flats

        return notesIntersectionScore + baseChordsScore + sharpnessScore
    }

    private val majorKeyNoteIndexes: Map<MajorKey, Set<Int>> by lazy {
        MajorKey.values().associateWith { majorKey ->
            majorKey.notes.map { it.index }.toSet()
        }
    }

    private val majorKeySharpnessAbs: Map<MajorKey, Int> by lazy {
        MajorKey.values().associateWith { majorKey ->
            majorKey.sharpness.absoluteValue
        }
    }

}

fun Boolean.toInt() = if (this) 1 else 0
