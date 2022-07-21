package igrek.songbook.chords.diagram.piano

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import igrek.songbook.chords.diagram.DrawableChordDiagramBuilder
import igrek.songbook.chords.model.GeneralChord
import igrek.songbook.chords.parser.ChordParser
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

    override fun buildDiagram(engChord: String): Bitmap? {

        val chord: GeneralChord = ChordParser(ChordsNotation.ENGLISH).parseGeneralChord(engChord) ?: return null

        val baseNoteIndex = chord.baseChord.noteIndex

        val notes = setOf(0, 4, 6, 7)

        val minNote = 0
        val maxNote: Int = notes.maxOf { it }

        val octaves: Int = max(1, (maxNote - 1) / 12 + 1)
        val whiteKeysVisible = octaves * 7 + 1
        val width = whiteKeysVisible * whiteKeyWidthPx

        val bitmap = Bitmap.createBitmap(width, diagramHeightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = colorWhite
        canvas.drawPaint(paint)

        drawKeys(canvas, notes, octaves)

        return bitmap
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
        val bottom = diagramHeightPx
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
        val supportedSuffixes = setOf(
            "+",
            "-",
            "-5",
            "-dur",
            "-moll",
            "0",
            "11",
            "11b9",
            "13",
            "13#11",
            "13b9",
            "2",
            "4",
            "4-3",
            "5",
            "5+",
            "6",
            "6+",
            "6-",
            "6-4",
            "6add9",
            "6add11",
            "7",
            "7#5",
            "7#9",
            "7(#5,#9)",
            "7(#5,b9)",
            "7(b5,#9)",
            "7(b5,b9)",
            "7+",
            "7/5+",
            "7/5-",
            "7b5",
            "7b9",
            "7sus2",
            "7sus4",
            "9",
            "9#5",
            "9b5",
            "9sus4",
            "add11",
            "add2",
            "add9",
            "aug",
            "b",
            "dim",
            "dim7",
            "m",
            "m+",
            "m11",
            "m13",
            "m5+",
            "m6",
            "m6+",
            "m6add9",
            "m7",
            "M7",
            "m7#5",
            "m7+",
            "m7/5-",
            "m7b5",
            "m9",
            "madd2",
            "madd9",
            "maj11",
            "maj13",
            "maj13#11",
            "maj7",
            "maj7#5",
            "maj7b5",
            "maj9",
            "maj9#11",
            "mmaj7",
            "mmaj9",
            "sus2",
            "sus2sus4",
            "sus4",
            "sus",
        )
    }

}