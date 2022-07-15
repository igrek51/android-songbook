package igrek.songbook.songpreview.renderer

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.recyclerview.widget.RecyclerView
import igrek.songbook.chords.lyrics.model.LyricsModel
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.settings.theme.ColorScheme
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.songpreview.quickmenu.QuickMenuAutoscroll
import igrek.songbook.songpreview.quickmenu.QuickMenuTranspose
import igrek.songbook.songpreview.renderer.canvas.BaseCanvasView
import igrek.songbook.system.WindowManagerService
import igrek.songbook.util.lookup.SimpleCache
import kotlin.math.abs
import kotlin.math.hypot


class SongPreview(
    context: Context,
    songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
    autoscrollService: LazyInject<AutoscrollService> = appFactory.autoscrollService,
    quickMenuTranspose: LazyInject<QuickMenuTranspose> = appFactory.quickMenuTranspose,
    quickMenuAutoscroll: LazyInject<QuickMenuAutoscroll> = appFactory.quickMenuAutoscroll,
    windowManagerService: LazyInject<WindowManagerService> = appFactory.windowManagerService,
    lyricsThemeService: LazyInject<LyricsThemeService> = appFactory.lyricsThemeService,
    playlistLayoutController: LazyInject<PlaylistLayoutController> = appFactory.playlistLayoutController,
) : BaseCanvasView(context), View.OnTouchListener {
    private val songPreviewController by LazyExtractor(songPreviewLayoutController)
    private val autoscroll by LazyExtractor(autoscrollService)
    private val quickMenuTranspose by LazyExtractor(quickMenuTranspose)
    private val quickMenuAutoscroll by LazyExtractor(quickMenuAutoscroll)
    private val windowManagerService by LazyExtractor(windowManagerService)
    private val lyricsThemeService by LazyExtractor(lyricsThemeService)
    private val playlistLayoutController by LazyExtractor(playlistLayoutController)

    private var lyricsModel: LyricsModel? = null
    var scroll: Float = 0f
        private set
    var scrollX: Float = 0f
        private set
    private var startScroll: Float = 0f
    private var startScrollX: Float = 0f
    private var fontsizeTmp: Float = 0f
    private var pointersDst0: Float? = null
    private var fontsize0: Float? = null
    private var lyricsRenderer: LyricsRenderer? = null
    private var startTouchY = 0f
    private var startTouchX = 0f
    private var startTouchScrollX = 0f
    private var startTouchTime: Long = 0
    private var multiTouch = false
    private val bottomMarginCache = SimpleCache {
        this.windowManagerService.dp2px(EOF_BOTTOM_RESERVE)
    }
    private val scrollThicknessCache = SimpleCache { this.windowManagerService.dp2px(4f) }
    private var lastClickTime: Long? = null
    private var recyclerScrollState = RecyclerView.SCROLL_STATE_IDLE
    var overlayScrollResetter: () -> Unit = {}
    var overlayScrollView: RecyclerView? = null

    companion object {
        const val EOF_BOTTOM_RESERVE = 60f // padding bottom [dp]
        const val LINEHEIGHT_SCALE_FACTOR = 1.02f
        const val FONTSIZE_SCALE_FACTOR = 0.6f
        const val DOUBLE_CLICK_INTERVAL: Long = 500 // [ms]
        const val GESTURE_HORIZONTAL_SWIPE = 0.25f // minimal factor of swiped screen width
    }

    private val fontsizePx: Float
        get() = windowManagerService.dp2px(this.fontsizeTmp)

    val lineheightPx: Float
        get() = fontsizePx * LINEHEIGHT_SCALE_FACTOR

    internal val maxScroll: Float
        get() {
            val bottomY = textBottomY.get()
            val reserve = bottomMarginCache.get()
            return if (bottomY > h) {
                bottomY + reserve - h
            } else {
                0f
            }
        }

    internal val maxScrollX: Float
        get() {
            return if (textRightX.get() > w) {
                textRightX.get() - w
            } else {
                0f
            }
        }

    private val textBottomY: SimpleCache<Float> = SimpleCache {
        if (lyricsModel == null)
            return@SimpleCache 0f
        val lines = lyricsModel?.lines
        if (lines.isNullOrEmpty())
            return@SimpleCache 0f
        val lineheight = lineheightPx
        lines.size * lineheight + lineheight
    }

    val visibleLinesAtEnd: Float
        get() {
            val bottom = textBottomY.get()
            return if (bottom < h) {
                bottom / lineheightPx
            } else {
                h / lineheightPx
            }
        }

    val visibleLines: Float
        get() {
            return textBottomY.get() / lineheightPx
        }

    private val textRightX: SimpleCache<Float> = SimpleCache {
        (lyricsModel?.lines?.maxOfOrNull { it.maxRightX() } ?: 0f) * fontsizePx
    }

    val scrollThickness: Float
        get() = scrollThicknessCache.get()

    val eyeFocusLines: Float
        get() = autoscroll.eyeFocusLines

    override fun reset() {
        super.reset()
        scroll = 0f
        scrollX = 0f
        startScroll = 0f
        startScrollX = 0f
        pointersDst0 = null
        fontsize0 = null
        lyricsModel = null
        textBottomY.invalidate()
        textRightX.invalidate()
    }

    override fun init() {
        songPreviewController.onGraphicsInitializedEvent(w, paint)
    }

    override fun onRepaint() {
        drawBackground()

        if (this.lyricsRenderer != null) {
            lyricsRenderer?.drawScrollBars()
            lyricsRenderer?.drawEyeFocusZone(lineheightPx)
            lyricsRenderer?.drawFileContent(fontsizePx, lineheightPx)
        }

        drawQuickMenuOverlay()
    }

    private fun drawBackground() {
        val backgroundColor = when (lyricsThemeService.colorScheme) {
            ColorScheme.DARK -> 0x000000
            ColorScheme.BRIGHT -> 0xf0f0f0
        }

        setColor(backgroundColor)
        clearScreen()
    }

    private fun drawQuickMenuOverlay() {
        if (quickMenuTranspose.isVisible) {
            //dimmed background
            setColor(0x000000, 110)
            fillRect(0f, 0f, w.toFloat(), h.toFloat())
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onTouchDown(event)
            MotionEvent.ACTION_MOVE -> onTouchMove(event)
            MotionEvent.ACTION_UP -> onTouchUp(event)
            MotionEvent.ACTION_POINTER_DOWN -> onTouchPointerDown(event)
            MotionEvent.ACTION_POINTER_UP -> onTouchPointerUp(event)
        }
        return false
    }

    private fun onTouchDown(event: MotionEvent) {
        startTouchY = event.y
        startTouchX = event.x
        startTouchScrollX = event.x
        startTouchTime = System.currentTimeMillis()
        startScroll = scroll
        startScrollX = scrollX
        pointersDst0 = null
        multiTouch = false
    }

    private fun onTouchMove(event: MotionEvent) {
        when {
            event.pointerCount == 1 -> {
                val dx = event.x - startTouchScrollX
                startTouchScrollX = event.x
                scrollByPxHorizontal(-dx)
            }
            event.pointerCount >= 2 -> {
                multiTouch = true
                // pinch to font scaling
                if (pointersDst0 != null) {
                    val pointersDst1 = hypot(
                        (event.getX(1) - event.getX(0)).toDouble(), (event.getY(1) - event
                            .getY(0)).toDouble()
                    ).toFloat()
                    val scale = (pointersDst1 / pointersDst0!! - 1) * FONTSIZE_SCALE_FACTOR + 1
                    val fontsize1 = fontsize0!! * scale
                    scroll = startScroll * scale
                    scrollX = startScrollX * scale
                    previewFontsize(fontsize1)
                }
            }
        }
    }

    private fun onTouchPointerDown(event: MotionEvent) {
        if (event.pointerCount >= 2)
            multiTouch = true
        pointersDst0 = hypot(
            (event.getX(1) - event.getX(0)).toDouble(),
            (event.getY(1) - event.getY(0)).toDouble()
        ).toFloat()
        fontsize0 = fontsizeTmp
        startScroll = scroll
        startScrollX = scrollX
    }

    private fun onTouchPointerUp(event: MotionEvent) {
        pointersDst0 = null // reset initial length
        startScroll = scroll
        startScrollX = scrollX
        // leave a pointer which is still active
        var pointerIndex = 0
        if (event.pointerCount >= 2) {
            for (i in 0 until event.pointerCount) {
                if (i != event.actionIndex) {
                    pointerIndex = i
                    break
                }
            }
        }
        startTouchY = event.getY(pointerIndex)

        songPreviewController.onFontsizeChangedEvent(fontsizeTmp)
    }

    private fun onTouchUp(event: MotionEvent) {
        if (!lyricsThemeService.horizontalScroll) {
            if (!multiTouch) {
                val dx = event.x - startTouchX
                val adx = abs(dx)
                if (adx > GESTURE_HORIZONTAL_SWIPE * w) {
                    if (dx > 0) {
                        playlistLayoutController.goToNextOrPrevious(-1)
                    } else {
                        playlistLayoutController.goToNextOrPrevious(+1)
                    }
                }
            }
        }
    }

    fun onClick() {
        val now = System.currentTimeMillis()
        if (songPreviewController.isQuickMenuVisible) {
            quickMenuTranspose.isVisible = false
            quickMenuAutoscroll.isVisible = false
            repaint()
        } else {
            if (autoscroll.isRunning) {
                autoscroll.onAutoscrollStopUIEvent()
                // reset double click
                lastClickTime = null
                return
            } else {
                // double tap
                if (lastClickTime != null && now - lastClickTime!! <= DOUBLE_CLICK_INTERVAL) {
                    onDoubleClick()
                }
            }
        }
        lastClickTime = now
    }

    private fun onDoubleClick() {
        autoscroll.onAutoscrollToggleUIEvent()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isInitialized) {
            songPreviewController.onPreviewSizeChange(w)
        }
    }

    fun setCRDModel(lyricsModel: LyricsModel?) {
        this.lyricsModel = lyricsModel
        textBottomY.invalidate()
        textRightX.invalidate()
        this.lyricsRenderer = LyricsRenderer(
            this,
            lyricsModel,
            lyricsThemeService.fontTypeface,
            lyricsThemeService.colorScheme,
            lyricsThemeService.displayStyle,
            lyricsThemeService.horizontalScroll,
        )
        repaint()
    }

    fun setFontSizes(fontsizeDp: Float) {
        this.fontsizeTmp = fontsizeDp
        textBottomY.invalidate()
        textRightX.invalidate()
    }

    private fun previewFontsize(fontsize1: Float) {
        val minScreen = if (w > h) h else w
        if (fontsize1 >= 5 && fontsize1 <= minScreen / 5) {
            setFontSizes(fontsize1)
            repaint()
        }
    }

    /**
     * @param lineheightPart lineheight part to move (em)
     * @return if it can be scrolled
     */
    fun scrollByLines(lineheightPart: Float): Boolean {
        return scrollByPxVertical(lineheightPart * lineheightPx)
    }

    fun changedRecyclerScrollState(recyclerScrollState: Int) {
        this.recyclerScrollState = recyclerScrollState
        scrollByPxVertical(0f)
    }

    fun scrollByPxVertical(py: Float): Boolean {
        val maxAbroad = lineheightPx * 6
        val stopRunawayScrollingMargin = lineheightPx * 5
        val boundaryHisteresis = 0.5f
        val minSettlingScrollPx = 0.035f
        when {
            scroll < 0 -> {
                val exceeds = -scroll
                when {
                    autoscroll.isRunning -> {
                        scroll = 0f
                    }

                    py < 0 && recyclerScrollState == RecyclerView.SCROLL_STATE_DRAGGING -> {
                        if (exceeds < maxAbroad) {
                            scroll += py * (1f - 1f / maxAbroad * exceeds)
                        }
                    }
                    py < 0 && recyclerScrollState == RecyclerView.SCROLL_STATE_SETTLING -> {
                        if (exceeds > stopRunawayScrollingMargin || -py / lineheightPx < minSettlingScrollPx) {
                            overlayScrollResetter()
                            overlayScrollView?.smoothScrollBy(
                                0,
                                exceeds.toInt(),
                                OvershootInterpolator(),
                                200
                            )
                        } else {
                            scroll += py * (1f - 1f / maxAbroad * exceeds)
                        }
                    }
                    recyclerScrollState == RecyclerView.SCROLL_STATE_IDLE -> {
                        if (scroll < -boundaryHisteresis)
                            overlayScrollView?.smoothScrollBy(
                                0,
                                exceeds.toInt(),
                                OvershootInterpolator(),
                                200
                            )
                    }
                    else -> scroll += py
                }
            }

            scroll > maxScroll -> {
                val exceeds = scroll - maxScroll
                when {
                    py > 0 && recyclerScrollState == RecyclerView.SCROLL_STATE_DRAGGING -> {
                        if (exceeds < maxAbroad) {
                            scroll += py * (1f - 1f / maxAbroad * exceeds)
                        }
                    }
                    py > 0 && recyclerScrollState == RecyclerView.SCROLL_STATE_SETTLING -> {
                        if (exceeds > stopRunawayScrollingMargin) {
                            overlayScrollResetter()
                            overlayScrollView?.smoothScrollBy(
                                0,
                                -exceeds.toInt(),
                                OvershootInterpolator(),
                                200
                            )
                        } else {
                            scroll += py * (1f - 1f / maxAbroad * exceeds)
                        }
                    }
                    recyclerScrollState == RecyclerView.SCROLL_STATE_IDLE -> {
                        if (scroll > maxScroll + boundaryHisteresis)
                            overlayScrollView?.smoothScrollBy(
                                0,
                                -exceeds.toInt(),
                                OvershootInterpolator(),
                                200
                            )
                    }
                    else -> scroll += py
                }
            }

            else -> scroll += py
        }

        val scrollable = when {
            scroll < 0 -> {
                if (autoscroll.isRunning)
                    scroll = 0f
                false
            }
            scroll >= maxScroll -> false
            else -> true
        }

        repaint()
        return scrollable
    }

    private fun scrollByPxHorizontal(px: Float) {
        if (!lyricsThemeService.horizontalScroll) {
            scrollX = 0f
            return
        }

        scrollX += px
        when {
            scrollX < 0f -> {
                scrollX = 0f
            }

            scrollX > maxScrollX -> {
                scrollX = maxScrollX
            }
        }

        repaint()
    }

    fun canScrollDown(): Boolean {
        return scroll < maxScroll
    }

    fun onManuallyScrolled(dy: Float) {
        // lines scrolled
        val linePartScrolled = dy / lineheightPx
        // monitor scroll changes\
        if (abs(linePartScrolled) > 0.01f) {
            autoscroll.canvasScrollSubject.onNext(linePartScrolled)
        }
    }

    fun goToBeginning() {
        scroll = 0f
        scrollX = 0f
        repaint()
    }
}
