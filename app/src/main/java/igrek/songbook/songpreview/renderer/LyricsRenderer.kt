package igrek.songbook.songpreview.renderer

import android.graphics.Typeface
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
    private var castFocusLineColor: Int
    private var castFocusZoneColor: Int

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
        castFocusLineColor = when (colorScheme) {
            ColorScheme.DARK -> 0xa03A82C5.toInt()
            ColorScheme.BRIGHT -> 0xa03A82C5.toInt()
        }
        castFocusZoneColor = when (colorScheme) {
            ColorScheme.DARK -> 0x703A82C5L.toInt()
            ColorScheme.BRIGHT -> 0x703A82C5L.toInt()
        }
    }

    fun drawAllLyrics(
        lineheightPx: Float,
        fontsizePx: Float,
        lyricsModel: LyricsModel,
        quickMenuVisible: Boolean,
    ) {
        drawBackground()

        drawScrollBars()
        if (songPreview.isCastPresentingSlides)
            drawCastFocusZone(lineheightPx, songPreview.scroll,
                preferencesState.castScrollControl.slideLines)
        if (preferencesState.autoscrollShowEyeFocus)
            drawEyeFocusZone(lineheightPx)
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
        val y = when (songPreview.isCastPresentingSlides) {
            false -> lineheight * lineIndex - scroll
            true -> lineheight * lineIndex - scroll + h / 2 - preferencesState.castScrollControl.slideLines * lineheight / 2
        }
        if (y > h) return
        if (y + lineheight < 0) return

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

        val thickness = songPreview.scrollThickness * 4f
        canvas.fillRectGradientH(
            w - thickness,
            eyeFocusTop,
            w,
            eyeFocusBottom,
            eyeFocusZoneColorTransparent,
            eyeFocusZoneColor
        )
    }

    private fun drawCastFocusZone(lineheight: Float, scroll: Float, slideLines: Int) {
        val yOffset: Float = 0.2f * lineheight
        val lineThickness = songPreview.scrollThickness
        val topEdge = h / 2 - slideLines * lineheight / 2
        val focusLineTop = topEdge - lineThickness / 2 + yOffset
        val focusLineBottom = topEdge + lineThickness / 2 + yOffset
        canvas.setColor(castFocusLineColor)
        canvas.fillRect(0f, focusLineTop, w, focusLineBottom)

        val topLineIndex = songPreview.castSlideMarkedLineTop
        val bottomLineIndex = songPreview.castSlideMarkedLineBottom
        val markedLinesNum = bottomLineIndex - topLineIndex + 1

        val blockYTop = topEdge + yOffset + topLineIndex * lineheight - scroll
        val blockYBottom = blockYTop + lineheight * markedLinesNum
        canvas.setColor(castFocusZoneColor)
        canvas.fillRect(0f, blockYTop, w, blockYBottom)
        canvas.borderRect(castFocusLineColor, 0f, blockYTop, w, blockYBottom, thickness=4f)
    }

    private fun drawQuickMenuOverlay() {
        canvas.setColor(0x000000, 110) //dimmed background
        canvas.fillRect(0f, 0f, canvas.w.toFloat(), canvas.h.toFloat())
    }

    fun drawSlides(
        slideCurrentIndex: Int,
        slideTargetIndex: Int,
        slideCurrentText: String,
        slideTargetText: String,
        slideAnimationProgress: Float,
        lineheightPx: Float,
        fontsizePx: Float,
    ) {
        drawBackground()

        val targetLines = slideTargetText.split("\n")
        val linesNum = targetLines.size

        canvas.setFontSize(fontsizePx)
        canvas.setFontTypeface(normalTypeface)
        canvas.setColor(textColor)

        targetLines.forEachIndexed { lineIndex, line ->
            val lineY = h/2 - linesNum * lineheightPx / 2 + lineIndex * lineheightPx
            canvas.drawText(
                line,
                w/2,
                lineY,
                Align.HCENTER,
            )
        }
    }
}
