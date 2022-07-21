package igrek.songbook.chords.diagram.piano

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import igrek.songbook.chords.diagram.DrawableChordDiagramBuilder
import igrek.songbook.chords.model.CompoundChord
import igrek.songbook.chords.model.GeneralChord
import igrek.songbook.chords.parser.ChordParser
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.settings.chordsnotation.ChordsNotation
import java.lang.Integer.max

class PianoChordDiagramBuilder : DrawableChordDiagramBuilder {

    private val whiteKeyWidthPx: Int = 32
    private val blackKeyWidthPx: Int = 18
    private val blackKeyHeightPx: Int = 75
    private val diagramHeightPx: Int = 120
    private val borderPx: Int = 1
    private val colorWhite: Int = (0xFFFFFFFF).toInt()
    private val colorBlack: Int = (0xFF000000).toInt()
    private val colorMarked: Int = (0xFFBF5751).toInt()

    private val logger = LoggerFactory.logger

    override fun buildDiagram(engChord: String): Bitmap? {
        val markedNotes: Set<Int>
        try {

            val chord: GeneralChord = ChordParser(ChordsNotation.ENGLISH).parseGeneralChord(engChord)
                ?: throw NoChordDiagramException("not a valid english chord")
            markedNotes = evaluateChordNotes(chord)

        } catch (e: NoChordDiagramException) {
            logger.warn("$e: $engChord")
            return null
        }

        val maxNote: Int = markedNotes.maxOf { it }
        val octaves: Int = max(1, (maxNote - 1) / 12 + 1)
        val whiteKeysVisible = octaves * 7 + 1
        val width = whiteKeysVisible * whiteKeyWidthPx

        val bitmap = Bitmap.createBitmap(width, diagramHeightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = colorWhite
        canvas.drawPaint(paint)

        drawKeys(canvas, markedNotes, octaves)

        return bitmap
    }

    override fun hasDiagram(chord: GeneralChord): Boolean {
        var suffix = chord.baseChord.suffix
        if (chord.baseChord.minor)
            suffix = "m$suffix"
        return suffix in chordTypeSequences.keys
    }

    private fun evaluateChordNotes(chord: GeneralChord): Set<Int> {
        var notes = mutableSetOf<Int>()
        val baseNote = chord.baseChord.noteIndex
        notes.add(baseNote)

        var suffix = chord.baseChord.suffix
        if (chord.baseChord.minor)
           suffix = "m$suffix"

        val sequence = chordTypeSequences[suffix] ?: throw NoChordDiagramException("chord has unsupported suffix")
        sequence.forEach { offset ->
            notes.add(baseNote + offset)
        }

        if (chord is CompoundChord) {
            if (chord.splitter == "/") {
                notes = applyChordInversion(notes, chord.chord2.noteIndex)
            }
        }

        return notes
    }

    private fun applyChordInversion(notes: Set<Int>, inversionNote: Int): MutableSet<Int> {
        val newNotes = mutableSetOf<Int>()
        var inverted = false
        notes.forEach { note ->
            if (note % 12 == inversionNote % 12) {
                newNotes.add(note)
                inverted = true
            } else {
                if (!inverted) {
                    newNotes.add(note + 12)
                } else {
                    newNotes.add(note)
                }
            }
        }

        if (!inverted)
            throw NoChordDiagramException("Inverted note not found in a chord")
        return newNotes
    }

    private fun drawKeys(canvas: Canvas, markedNotes: Set<Int>, octaves: Int) {
        val allNotes = octaves * 12 + 2

        val blackPaint = Paint().apply {
            color = colorBlack
            strokeWidth = borderPx.toFloat()
        }
        val markPaint = Paint().apply {
            color = colorMarked
            style = Paint.Style.FILL
        }

        // mark white keys
        for (noteIndex in 0 until allNotes) {
            if (noteIndex in markedNotes) {
                noteIndexToWhiteIndex(noteIndex)?.let { whiteIndex ->
                    drawWhiteKey(canvas, markPaint, whiteIndex)
                }
            }
        }

        // white keys
        blackPaint.style = Paint.Style.STROKE
        for (noteIndex in 0 until allNotes) {
            noteIndexToWhiteIndex(noteIndex)?.let { whiteIndex ->
                drawWhiteKey(canvas, blackPaint, whiteIndex)
            }
        }

        // black keys
        blackPaint.style = Paint.Style.FILL
        for (noteIndex in 0 until allNotes) {
            noteIndexToBlackIndex(noteIndex)?.let { blackIndex ->
                drawBlackKey(canvas, blackPaint, blackIndex)
            }
        }

        // mark black keys
        for (noteIndex in 0 until allNotes) {
            if (noteIndex in markedNotes) {
                noteIndexToBlackIndex(noteIndex)?.let { blackIndex ->
                    drawBlackKey(canvas, markPaint, blackIndex, background = true)
                }
            }
        }
    }

    private fun drawWhiteKey(canvas: Canvas, paint: Paint, whiteIndex: Int) {
        val left = whiteKeyWidthPx * whiteIndex
        val right = left + whiteKeyWidthPx
        val top = 0
        val bottom = diagramHeightPx - 1
        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
    }

    private fun drawBlackKey(canvas: Canvas, paint: Paint, whiteIndex: Int, background: Boolean = false) {
        var left = whiteKeyWidthPx * (whiteIndex + 1) - blackKeyWidthPx / 2
        var right = whiteKeyWidthPx * (whiteIndex + 1) + blackKeyWidthPx / 2
        var top = 0
        var bottom = blackKeyHeightPx
        if (background) {
            left += borderPx
            right -= borderPx
            top += borderPx
            bottom -= borderPx
        }
        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
    }

    private fun noteIndexToWhiteIndex(noteIndex: Int): Int? {
        val octaveIndex = noteIndex / 12
        return when (noteIndex % 12) {
            0 -> octaveIndex * 7 + 0 // C
            2 -> octaveIndex * 7 + 1 // D
            4 -> octaveIndex * 7 + 2 // E
            5 -> octaveIndex * 7 + 3 // F
            7 -> octaveIndex * 7 + 4 // G
            9 -> octaveIndex * 7 + 5 // A
            11 -> octaveIndex * 7 + 6 // B
            else -> null
        }
    }

    private fun noteIndexToBlackIndex(noteIndex: Int): Int? {
        val octaveIndex = noteIndex / 12
        return when (noteIndex % 12) {
            1 -> octaveIndex * 7 + 0 // C#
            3 -> octaveIndex * 7 + 1 // D#
            6 -> octaveIndex * 7 + 3 // F#
            8 -> octaveIndex * 7 + 4 // G#
            10 -> octaveIndex * 7 + 5 // A#
            else -> null
        }
    }

    companion object {
        val chordTypeSequences: Map<String, List<Int>> = mapOf(
            "" to listOf(0, 4, 7), // Major
            "m" to listOf(0, 3, 7), // minor
            "7" to listOf(0, 4, 7, 10),
            "m7" to listOf(0, 3, 7, 10),
            "maj7" to listOf(0, 4, 7, 11),
            "6" to listOf(0, 4, 7, 9),
            "5" to listOf(0, 7),
            "sus2" to listOf(0, 2, 7),
            "sus4" to listOf(0, 5, 7),
            "dim" to listOf(0, 3, 6),
            "0" to listOf(0, 3, 6),
            "aug" to listOf(0, 4, 8),
            "add2" to listOf(0, 2, 4, 7),
            "mM7" to listOf(0, 3, 7, 11),
            "mMaj7" to listOf(0, 3, 7, 11),
            "m(M7)" to listOf(0, 3, 7, 11),
            "m6" to listOf(0, 3, 7, 9),
            "6/9" to listOf(0, 4, 7, 9, 14),
            "6add9" to listOf(0, 4, 7, 9, 14),
            "9" to listOf(0, 4, 7, 10, 14),
            "m9" to listOf(0, 3, 7, 10, 14),
            "maj9" to listOf(0, 4, 7, 11, 14),
            "11" to listOf(0, 4, 7, 10, 14, 17),
            "m11" to listOf(0, 3, 7, 10, 14, 17),
            "13" to listOf(0, 4, 7, 10, 14, 17, 21),
            "m13" to listOf(0, 3, 7, 10, 14, 17, 21),
            "maj13" to listOf(0, 4, 7, 11, 14, 21),
            "add9" to listOf(0, 4, 7, 14),
            "add11" to listOf(0, 4, 7, 17),
            "add4" to listOf(0, 4, 5, 7),
            "7-5" to listOf(0, 4, 6, 10),
            "7b5" to listOf(0, 4, 6, 10),
            "7+5" to listOf(0, 4, 8, 10),
            "7#5" to listOf(0, 4, 8, 10),
            "7sus4" to listOf(0, 5, 7, 10),
            "9sus4" to listOf(0, 5, 7, 10),
            "dim7" to listOf(0, 3, 6, 9),
            "07" to listOf(0, 3, 6, 9),
            "m7b5" to listOf(0, 3, 6, 10),
            "m7(b5)" to listOf(0, 3, 6, 10),
            "m7-b5" to listOf(0, 3, 6, 10),
            "aug" to listOf(0, 4, 8),
            "+" to listOf(0, 4, 8),
            "aug7" to listOf(0, 4, 8, 10),
            "+7" to listOf(0, 4, 8, 10),
            "7+" to listOf(0, 4, 8, 10),
            "7#5" to listOf(0, 4, 8, 10),
            "mM9" to listOf(0, 3, 7, 11, 14),
            "mMaj9" to listOf(0, 3, 7, 11, 14),
            "m6/9" to listOf(0, 3, 7, 9, 14),
            "m6add9" to listOf(0, 3, 7, 9, 14),
        )
    }

    class NoChordDiagramException(message: String) : RuntimeException(message)

}