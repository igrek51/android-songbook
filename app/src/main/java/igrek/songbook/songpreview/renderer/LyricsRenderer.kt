package igrek.songbook.songpreview.renderer

import android.graphics.Typeface
import igrek.songbook.model.lyrics.LyricsLine
import igrek.songbook.model.lyrics.LyricsModel
import igrek.songbook.model.lyrics.LyricsTextType
import igrek.songbook.songpreview.renderer.canvas.Align
import igrek.songbook.songpreview.theme.FontTypeface

class LyricsRenderer internal constructor(private val canvas: SongPreview,
                                          private val lyricsModel: LyricsModel?,
                                          fontTypeface: FontTypeface) {

    private val w: Float = canvas.w.toFloat()
    private val h: Float = canvas.h.toFloat()

    private var normalTypeface: Typeface? = null
    private var boldTypeface: Typeface? = null

    init {
        val typefaceFamily = fontTypeface.typeface
        normalTypeface = Typeface.create(typefaceFamily, Typeface.NORMAL)
        boldTypeface = Typeface.create(typefaceFamily, Typeface.BOLD)
    }

    /**
     * @param fontsize   fontsize in pixels
     * @param lineheight
     */
    fun drawFileContent(fontsize: Float, lineheight: Float) {
        canvas.setFontSize(fontsize)
        canvas.setColor(0xffffff)

        if (lyricsModel != null) {
            for (line in lyricsModel.lines) {
                drawTextLine(line, canvas.scroll, fontsize, lineheight)
            }
        }
    }

    private fun drawTextLine(line: LyricsLine, scroll: Float, fontsize: Float, lineheight: Float) {
        val y = lineheight * line.y - scroll
        if (y > h)
            return
        if (y + lineheight < 0)
            return

        // line wrapper on bottom layer
        if (line.fragments.size > 0) {
            val lastFragment = line.fragments[line.fragments.size - 1]
            if (lastFragment.type == LyricsTextType.LINEWRAPPER) {
                canvas.setFontTypeface(normalTypeface)
                canvas.setColor(0x707070)
                canvas.drawText(lastFragment.text, w, y + 0.9f * lineheight, Align.RIGHT)
            }
        }

        for (fragment in line.fragments) {

            if (fragment.type == LyricsTextType.REGULAR_TEXT) {
                canvas.setFontTypeface(normalTypeface)
                canvas.setColor(0xffffff)
                canvas.drawText(fragment.text, fragment.x * fontsize, y + lineheight, Align.LEFT)
            } else if (fragment.type == LyricsTextType.CHORDS) {
                canvas.setFontTypeface(boldTypeface)
                canvas.setColor(0xf00000)
                canvas.drawText(fragment.text, fragment.x * fontsize, y + lineheight, Align.LEFT)
            }

        }
    }

    fun drawScrollBar() {
        val scroll = canvas.scroll
        val maxScroll = canvas.maxScroll
        val range = maxScroll + h
        val top = scroll / range
        val bottom = (scroll + h) / range

        canvas.setColor(0xAEC3E0)
        val scrollWidth = canvas.scrollWidth
        canvas.fillRect(w - scrollWidth, top * h, w, bottom * h)
    }

}
