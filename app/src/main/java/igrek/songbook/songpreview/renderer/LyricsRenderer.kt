package igrek.songbook.songpreview.renderer

import android.graphics.Typeface
import android.os.Build
import igrek.songbook.chords.model.LyricsLine
import igrek.songbook.chords.model.LyricsModel
import igrek.songbook.chords.model.LyricsTextType
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.DisplayStyle
import igrek.songbook.settings.theme.FontTypeface
import igrek.songbook.songpreview.renderer.canvas.Align

class LyricsRenderer internal constructor(
    private val canvas: SongPreview,
    private val lyricsModel: LyricsModel,
    fontTypeface: FontTypeface,
    colorScheme: ColorScheme,
    private val displayStyle: DisplayStyle,
    private val horizontalScroll: Boolean,
) {

    private val w: Float = canvas.w.toFloat()
    private val h: Float = canvas.h.toFloat()

    private var normalTypeface: Typeface? = null
    private var boldTypeface: Typeface? = null
    private var italicTypeface: Typeface? = null
    private var textColor: Int
    private var chordColor: Int
    private var commentColor: Int
    private var linewrapperColor: Int
    private var scrollColor: Int
    private var eyeFocusZoneColor: Int
    private var eyeFocusZoneColorTransparent: Int

    init {
        val typefaceFamily = fontTypeface.typeface
        normalTypeface = Typeface.create(typefaceFamily, Typeface.NORMAL)
        boldTypeface = Typeface.create(typefaceFamily, Typeface.BOLD)
        italicTypeface = Typeface.create(typefaceFamily, Typeface.ITALIC)

        textColor = when (colorScheme) {
            ColorScheme.DARK -> 0xffffff
            ColorScheme.BRIGHT -> 0x000000
        }
        chordColor = when (colorScheme) {
            ColorScheme.DARK -> 0xf00000
            ColorScheme.BRIGHT -> 0xf00000
        }
        commentColor = when (colorScheme) {
            ColorScheme.DARK -> 0x929292
            ColorScheme.BRIGHT -> 0x6D6D6D
        }
        linewrapperColor = when (colorScheme) {
            ColorScheme.DARK -> 0x707070
            ColorScheme.BRIGHT -> 0x707070
        }
        scrollColor = when (colorScheme) {
            ColorScheme.DARK -> 0x404040
            ColorScheme.BRIGHT -> 0xAAAAAA
        }
        eyeFocusZoneColor = when (colorScheme) {
            ColorScheme.DARK -> 0xd03A82C5.toInt()
            ColorScheme.BRIGHT -> 0xd03A82C5.toInt()
        }
        eyeFocusZoneColorTransparent = when (colorScheme) {
            ColorScheme.DARK -> 0x003A82C5
            ColorScheme.BRIGHT -> 0x003A82C5
        }
    }

    /**
     * @param fontsize   fontsize in pixels
     * @param lineheight
     */
    fun drawFileContent(fontsize: Float, lineheight: Float) {
        canvas.setFontSize(fontsize)
        lyricsModel.lines.forEachIndexed { lineIndex, line ->
            drawTextLine(line, canvas.scroll, canvas.scrollX, fontsize, lineheight, lineIndex)
        }
    }

    private fun drawTextLine(
        line: LyricsLine,
        scroll: Float,
        scrollX: Float,
        fontsize: Float,
        lineheight: Float,
        lineIndex: Int,
    ) {
        val y = lineheight * lineIndex - scroll
        if (y > h)
            return
        if (y + lineheight < 0)
            return

        // line wrapper on bottom layer
        if (line.fragments.isNotEmpty()) {
            val lastFragment = line.fragments[line.fragments.size - 1]
            if (lastFragment.type == LyricsTextType.LINEWRAPPER) {
                canvas.setFontTypeface(normalTypeface)
                canvas.setColor(linewrapperColor)
                canvas.drawText(lastFragment.text, w - 4, y + 0.9f * lineheight, Align.RIGHT)
            }
        }

        for (fragment in line.fragments) {
            when (fragment.type) {
                LyricsTextType.REGULAR_TEXT -> {
                    canvas.setFontTypeface(normalTypeface)
                    canvas.setColor(textColor)
                    canvas.drawText(
                        fragment.text,
                        fragment.x * fontsize - scrollX,
                        y + lineheight,
                        Align.LEFT
                    )
                }
                LyricsTextType.CHORDS -> {
                    canvas.setFontTypeface(boldTypeface)
                    canvas.setColor(chordColor)
                    val x = if (displayStyle == DisplayStyle.ChordsAlignedRight) {
                        fragment.x * fontsize - canvas.scrollThickness - scrollX
                    } else {
                        fragment.x * fontsize - scrollX
                    }
                    canvas.drawText(fragment.text, x, y + lineheight, Align.LEFT)
                }
                LyricsTextType.COMMENT -> {
                    canvas.setFontTypeface(italicTypeface)
                    canvas.setColor(commentColor)
                    canvas.drawText(
                        fragment.text,
                        fragment.x * fontsize - scrollX,
                        y + lineheight,
                        Align.LEFT
                    )
                }
                LyricsTextType.LINEWRAPPER -> {}
            }
        }
    }

    fun drawScrollBars() {
        //vertical scrollbar
        val scroll = canvas.scroll
        val maxScroll = canvas.maxScroll
        val range = maxScroll + h
        val top = scroll / range
        val bottom = (scroll + h) / range

        canvas.setColor(scrollColor)
        val scrollThickness = canvas.scrollThickness
        canvas.fillRect(w - scrollThickness, top * h, w, bottom * h)

        //horizontal scrollbar
        if (horizontalScroll) {
            val maxScrollX = canvas.maxScrollX
            if (maxScrollX > 0) {
                val scrollX = canvas.scrollX
                val rangeX = maxScrollX + w
                val left = scrollX / rangeX
                val right = (scrollX + w) / rangeX
                canvas.fillRect(left * w, h - scrollThickness, right * w, h)
            }
        }
    }

    fun drawEyeFocusZone(lineheight: Float) {
        val eyeFocusLines = canvas.eyeFocusLines
        if (eyeFocusLines <= 0f)
            return

        val eyeFocusTop = (eyeFocusLines - 1f) * lineheight - canvas.scroll
        val eyeFocusBottom = eyeFocusTop + 2f * lineheight

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val thickness = canvas.scrollThickness * 4f
            canvas.fillRectGradientH(
                w - thickness,
                eyeFocusTop,
                w,
                eyeFocusBottom,
                eyeFocusZoneColorTransparent,
                eyeFocusZoneColor
            )
        } else {
            val thickness = canvas.scrollThickness * 2f
            canvas.setColor(eyeFocusZoneColor)
            canvas.fillRect(w - thickness, eyeFocusTop, w, eyeFocusBottom)
        }
    }

}
