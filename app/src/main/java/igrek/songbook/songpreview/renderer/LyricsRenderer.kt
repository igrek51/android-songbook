package igrek.songbook.songpreview.renderer

import android.graphics.Typeface
import android.os.Build
import igrek.songbook.chords.model.LyricsLine
import igrek.songbook.chords.model.LyricsModel
import igrek.songbook.chords.model.LyricsTextType
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.DisplayStyle
import igrek.songbook.songpreview.renderer.canvas.Align
import igrek.songbook.songpreview.renderer.canvas.CanvasView

class LyricsRenderer internal constructor(
    private val songPreview: SongPreview,
    private val canvas: CanvasView,
    private val preferencesState: PreferencesState,
) {
    private val w: Float get() = canvas.w.toFloat()
    private val h: Float get() = canvas.h.toFloat()

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
        val typefaceFamily = preferencesState.fontTypeface.typeface
        normalTypeface = Typeface.create(typefaceFamily, Typeface.NORMAL)
        boldTypeface = Typeface.create(typefaceFamily, Typeface.BOLD)
        italicTypeface = Typeface.create(typefaceFamily, Typeface.ITALIC)

        val colorScheme = preferencesState.colorScheme
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

    fun drawAll(
        lineheightPx: Float,
        fontsizePx: Float,
        lyricsModel: LyricsModel,
        quickMenuVisible: Boolean,
    ) {
        drawBackground()

        drawScrollBars()
        if (preferencesState.autoscrollShowEyeFocus) {
            drawEyeFocusZone(lineheightPx)
        }
        if (preferencesState.castFocusControl.slide) {
            drawCastFocusZone(lineheightPx)
        }
        drawFileContent(lyricsModel, fontsizePx, lineheightPx)

        if (quickMenuVisible) {
            drawQuickMenuOverlay()
        }
    }

    private fun drawBackground() {
        val backgroundColor = when (preferencesState.colorScheme) {
            ColorScheme.DARK -> 0x000000
            ColorScheme.BRIGHT -> 0xf0f0f0
        }
        canvas.setColor(backgroundColor)
        canvas.clearScreen()
    }

    /**
     * @param fontsize   fontsize in pixels
     * @param lineheight
     */
    private fun drawFileContent(lyricsModel: LyricsModel, fontsize: Float, lineheight: Float) {
        canvas.setFontSize(fontsize)
        lyricsModel.lines.forEachIndexed { lineIndex, line ->
            drawTextLine(line, songPreview.scroll, songPreview.scrollX, fontsize, lineheight, lineIndex)
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
        val y = when (preferencesState.castFocusControl.slide) {
            false -> lineheight * lineIndex - scroll
            true -> lineheight * lineIndex - scroll + h / 2
        }
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
                        Align.LEFT,
                    )
                }
                LyricsTextType.CHORDS -> {
                    canvas.setFontTypeface(boldTypeface)
                    canvas.setColor(chordColor)
                    val x = if (preferencesState.chordsDisplayStyle == DisplayStyle.ChordsAlignedRight) {
                        fragment.x * fontsize - songPreview.scrollThickness - scrollX
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
                        Align.LEFT,
                    )
                }
                LyricsTextType.LINEWRAPPER -> {}
            }
        }
    }

    private fun drawScrollBars() {
        //vertical scrollbar
        val scroll = songPreview.scroll
        val maxScroll = songPreview.maxScroll
        val range = maxScroll + h
        val top = scroll / range
        val bottom = (scroll + h) / range

        canvas.setColor(scrollColor)
        val scrollThickness = songPreview.scrollThickness
        canvas.fillRect(w - scrollThickness, top * h, w, bottom * h)

        //horizontal scrollbar
        if (preferencesState.horizontalScroll) {
            val maxScrollX = songPreview.maxScrollX
            if (maxScrollX > 0) {
                val scrollX = canvas.scrollX
                val rangeX = maxScrollX + w
                val left = scrollX / rangeX
                val right = (scrollX + w) / rangeX
                canvas.fillRect(left * w, h - scrollThickness, right * w, h)
            }
        }
    }

    private fun drawEyeFocusZone(lineheight: Float) {
        val eyeFocusLines = songPreview.eyeFocusLines
        if (eyeFocusLines <= 0f)
            return

        val eyeFocusTop = (eyeFocusLines - 1f) * lineheight - songPreview.scroll
        val eyeFocusBottom = eyeFocusTop + 2f * lineheight

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val thickness = songPreview.scrollThickness * 4f
            canvas.fillRectGradientH(
                w - thickness,
                eyeFocusTop,
                w,
                eyeFocusBottom,
                eyeFocusZoneColorTransparent,
                eyeFocusZoneColor
            )
        } else {
            val thickness = songPreview.scrollThickness * 2f
            canvas.setColor(eyeFocusZoneColor)
            canvas.fillRect(w - thickness, eyeFocusTop, w, eyeFocusBottom)
        }
    }

    private fun drawCastFocusZone(lineheight: Float) {
        val eyeFocusTop = h / 2 - lineheight / 2
        val eyeFocusBottom = h / 2 + lineheight / 2

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            canvas.fillRectGradientH(
                0f,
                eyeFocusTop,
                w,
                eyeFocusBottom,
                eyeFocusZoneColorTransparent,
                eyeFocusZoneColor,
            )
        } else {
            canvas.setColor(eyeFocusZoneColor)
            canvas.fillRect(0f, eyeFocusTop, w, eyeFocusBottom)
        }
    }

    private fun drawQuickMenuOverlay() {
        //dimmed background
        canvas.setColor(0x000000, 110)
        canvas.fillRect(0f, 0f, canvas.w.toFloat(), canvas.h.toFloat())
    }
}
