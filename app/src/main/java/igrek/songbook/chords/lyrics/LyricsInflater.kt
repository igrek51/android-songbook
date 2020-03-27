package igrek.songbook.chords.lyrics

import android.graphics.Paint
import android.graphics.Typeface
import igrek.songbook.chords.lyrics.model.LyricsFragment
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.lyrics.model.LyricsTextType
import igrek.songbook.chords.lyrics.model.lineWrapperChar

class LyricsInflater(
        fontFamily: Typeface,
        private val fontsize: Float
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
            LyricsTextType.CHORDS -> boldPaint
            else -> return 0f
        }

        val widths = FloatArray(text.length)
        paint.getTextWidths(text, widths)
        widths.forEachIndexed { index, width ->
            lengthMapper.put(type, text[index], width / fontsize)
        }

        return widths.sum() / fontsize
    }

    private fun inflateFragment(fragment: LyricsFragment): Float {
        fragment.width = calculateTextWidth(fragment.text, fragment.type)
        return fragment.width
    }

}
