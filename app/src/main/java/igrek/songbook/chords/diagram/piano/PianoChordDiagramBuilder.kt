package igrek.songbook.chords.diagram.piano

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import igrek.songbook.chords.diagram.DrawableChordDiagramBuilder

class PianoChordDiagramBuilder : DrawableChordDiagramBuilder {

    override fun buildDiagram(engChord: String, context: Context): Bitmap {
        val x = 10f
        val y = 10f
        val r = 4f
        val w = 2000
        val h = 300

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.style = Paint.Style.FILL

        var color: Long = 0xFFFFFFFF
        paint.color = color.toInt()
        canvas.drawPaint(paint)

        color = 0xFFFF0000
        paint.color = color.toInt()
        canvas.drawCircle(x, y, r, paint)

        return bitmap
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