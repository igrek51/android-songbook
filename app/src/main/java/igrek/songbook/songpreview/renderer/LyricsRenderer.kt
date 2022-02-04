package igrek.songbook.songpreview.renderer

import android.graphics.Typeface
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.lyrics.model.LyricsTextType
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.DisplayStyle
import igrek.songbook.settings.theme.FontTypeface
import igrek.songbook.songpreview.renderer.canvas.Align

class LyricsRenderer internal constructor(
    private val canvas: SongPreview,
    private val lyricsModel: LyricsModel?,
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
            ColorScheme.DARK -> 0x4A4A4A
            ColorScheme.BRIGHT -> 0xA0A0A0
        }
    }

    /**
     * @param fontsize   fontsize in pixels
     * @param lineheight
     */
    fun drawFileContent(fontsize: Float, lineheight: Float) {
        canvas.setFontSize(fontsize)
        lyricsModel?.lines?.forEachIndexed { lineIndex, line ->
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
                canvas.drawText(lastFragment.text, w - 5, y + 0.9f * lineheight, Align.RIGHT)
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

        canvas.setColor(scrollColor.toInt())
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

}
