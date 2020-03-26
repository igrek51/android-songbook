package igrek.songbook.chords.lyrics

import android.graphics.Paint
import android.graphics.Typeface
import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.lyrics.model.LyricsTextType

class LyricsInflater(
        fontFamily: Typeface,
        private val fontsize: Float
) {

    private val normalTypeface: Typeface = Typeface.create(fontFamily, Typeface.NORMAL)
    private val boldTypeface: Typeface = Typeface.create(fontFamily, Typeface.BOLD)
    private val normalPaint = buildPaint(normalTypeface)
    private val boldPaint = buildPaint(boldTypeface)
    private val boldSpaceWidth = calculateTextWidth(boldPaint, " ")

    fun inflateLyrics(model: LyricsModel): LyricsModel {
        model.lines.forEach { line ->
            var x = 0f
            line.fragments.forEach { fragment ->
                x += inflateFragment(fragment, x)
            }
        }
        return model
    }

    private fun buildPaint(typeface: Typeface): Paint {
        return Paint().apply {
            this.isAntiAlias = true
            this.isFilterBitmap = true
            this.textSize = fontsize
            this.typeface = typeface
        }
    }

    private fun calculateTextWidth(paint: Paint, text: String): Float {
        val widths = FloatArray(text.length)
        paint.getTextWidths(text, widths)
        return widths.sum()
    }

    private fun inflateFragment(fragment: LyricsFragment, x: Float): Float {
        val paint = when (fragment.type) {
            LyricsTextType.REGULAR_TEXT -> normalPaint
            LyricsTextType.CHORDS -> boldPaint
            else -> return 0f
        }

        fragment.x = x
        fragment.width = calculateTextWidth(paint, fragment.text)
        return fragment.width
    }

}
