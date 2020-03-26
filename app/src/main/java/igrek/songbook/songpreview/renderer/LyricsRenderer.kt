package igrek.songbook.songpreview.renderer

import android.graphics.Typeface
import igrek.songbook.chords.lyrics.model.LyricsLine
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.chords.lyrics.model.LyricsTextType
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.FontTypeface
import igrek.songbook.songpreview.renderer.canvas.Align

class LyricsRenderer internal constructor(private val canvas: SongPreview,
                                          private val lyricsModel: LyricsModel?,
                                          fontTypeface: FontTypeface,
                                          colorScheme: ColorScheme,
                                          private val chordsEndOfLine: Boolean) {

    private val w: Float = canvas.w.toFloat()
    private val h: Float = canvas.h.toFloat()

    private var normalTypeface: Typeface? = null
    private var boldTypeface: Typeface? = null
    private var textColor: Int
    private var chordColor: Int
    private var linewrapperColor: Int

    init {
        val typefaceFamily = fontTypeface.typeface
        normalTypeface = Typeface.create(typefaceFamily, Typeface.NORMAL)
        boldTypeface = Typeface.create(typefaceFamily, Typeface.BOLD)

        textColor = when (colorScheme) {
            ColorScheme.DARK -> 0xffffff
            ColorScheme.BRIGHT -> 0x000000
        }
        chordColor = when (colorScheme) {
            ColorScheme.DARK -> 0xf00000
            ColorScheme.BRIGHT -> 0xf00000
        }
        linewrapperColor = when (colorScheme) {
            ColorScheme.DARK -> 0x707070
            ColorScheme.BRIGHT -> 0x707070
        }
    }

    /**
     * @param fontsize   fontsize in pixels
     * @param lineheight
     */
    fun drawFileContent(fontsize: Float, lineheight: Float) {
        canvas.setFontSize(fontsize)
        lyricsModel?.lines?.forEachIndexed { lineIndex, line ->
            drawTextLine(line, canvas.scroll, fontsize, lineheight, lineIndex)
        }
    }

    private fun drawTextLine(line: LyricsLine, scroll: Float, fontsize: Float, lineheight: Float, lineIndex: Int) {
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
                canvas.drawText(lastFragment.text, w, y + 0.9f * lineheight, Align.RIGHT)
            }
        }

        for (fragment in line.fragments) {

            if (fragment.type == LyricsTextType.REGULAR_TEXT) {
                canvas.setFontTypeface(normalTypeface)
                canvas.setColor(textColor)
                canvas.drawText(fragment.text, fragment.x * fontsize, y + lineheight, Align.LEFT)
            } else if (fragment.type == LyricsTextType.CHORDS) {
                canvas.setFontTypeface(boldTypeface)
                canvas.setColor(chordColor)
                val x = if (chordsEndOfLine) {
                    fragment.x * fontsize - canvas.scrollWidth
                } else {
                    fragment.x * fontsize
                }
                canvas.drawText(fragment.text, x, y + lineheight, Align.LEFT)
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
