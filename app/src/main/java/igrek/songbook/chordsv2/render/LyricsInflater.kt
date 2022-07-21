package igrek.songbook.chordsv2.render

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import igrek.songbook.chordsv2.model.LyricsFragment
import igrek.songbook.chordsv2.model.LyricsModel
import igrek.songbook.chordsv2.model.LyricsTextType
import igrek.songbook.chordsv2.model.lineWrapperChar
import igrek.songbook.info.logger.LoggerFactory.logger

class LyricsInflater(
    fontFamily: Typeface,
    private val fontsize: Float,
) {

    private val normalTypeface: Typeface = Typeface.create(fontFamily, Typeface.NORMAL)
    private val boldTypeface: Typeface = Typeface.create(fontFamily, Typeface.BOLD)
    private val normalPaint = buildPaint(normalTypeface)
    private val boldPaint = buildPaint(boldTypeface)
    val lengthMapper = TypefaceLengthMapper()

    fun inflateLyrics(model: LyricsModel): LyricsModel {
        calculateTextWidth(" ", LyricsTextType.REGULAR_TEXT)
        calculateTextWidth(" ", LyricsTextType.CHORDS)
        calculateTextWidth(lineWrapperChar.toString(), LyricsTextType.REGULAR_TEXT)

        model.lines.forEach { line ->
            line.fragments.forEach { fragment ->
                inflateFragment(fragment)
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

    private fun calculateTextWidth(text: String, type: LyricsTextType): Float {
        val paint = when (type) {
            LyricsTextType.REGULAR_TEXT -> normalPaint
            LyricsTextType.COMMENT -> normalPaint
            LyricsTextType.CHORDS -> boldPaint
            else -> return 0f
        }

        val widths = FloatArray(text.length)
        paint.getTextWidths(text, widths)
        widths.forEachIndexed { index, width ->
            val char = text[index]
            if (!lengthMapper.has(type, char)) {
                if (width <= 0f) { // workaround for fucked up android library
                    val widthSingle = calculateCharWidthFallback(char, paint)
                    if (widthSingle > 0f) {
                        lengthMapper.put(type, char, widthSingle / fontsize)
                    }
                } else {
                    lengthMapper.put(type, char, width / fontsize)
                }
            }
        }

        return widths.sum() / fontsize
    }

    private fun calculateCharWidthFallback(char: Char, paint: Paint): Float {
        val widthsSingle = FloatArray(1)
        paint.getTextWidths(char.toString(), widthsSingle)
        val widthSingle = widthsSingle[0]
        if (widthSingle > 0f)
            return widthSingle

        logger.warn("character width still zero: \"$char\"")
        val bounds = Rect()
        paint.getTextBounds(char.toString(), 0, 1, bounds)
        return bounds.width().toFloat()
    }

    private fun inflateFragment(fragment: LyricsFragment): Float {
        fragment.width = calculateTextWidth(fragment.text, fragment.type)
        return fragment.width
    }

}
