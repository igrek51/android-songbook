package igrek.songbook.chords.detect

import igrek.songbook.chords.model.*
import igrek.songbook.chords.syntax.MajorKey
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
        val notesIntersectionScore = (2 * notesIntersection.size - uniqueNotes.size) * 1000

        val hasTonic = (key.tonic() in majorChordNotes).toInt() // major I
        val hasSubmediant = (key.submediant() in minorChordNotes).toInt() // minor (vi)
        val hasDominant = (key.dominant() in majorChordNotes).toInt() // V
        val hasSubdominant = (key.subdominant() in majorChordNotes).toInt() // IV
        val hasMediant = (key.mediant() in minorChordNotes).toInt() // iii
        val hasSupertonic = (key.supertonic() in minorChordNotes).toInt() // ii
        val scaleDegreeScore = hasTonic * 500 + hasSubmediant * 250 + hasDominant * 125 + hasSubdominant * 50 + hasMediant * 10 + hasSupertonic * 10

        val sharpnessAbs = majorKeySharpnessAbs[key]!!
        val sharpnessScore = -sharpnessAbs // take key with less sharps/flats

        return notesIntersectionScore + scaleDegreeScore + sharpnessScore
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
