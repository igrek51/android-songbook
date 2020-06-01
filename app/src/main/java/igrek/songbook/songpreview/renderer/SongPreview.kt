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
) : BaseCanvasView(context), View.OnTouchListener {
    private val songPreviewController by LazyExtractor(songPreviewLayoutController)
    private val autoscroll by LazyExtractor(autoscrollService)
    private val quickMenuTranspose by LazyExtractor(quickMenuTranspose)
    private val quickMenuAutoscroll by LazyExtractor(quickMenuAutoscroll)
    private val windowManagerService by LazyExtractor(windowManagerService)
    private val lyricsThemeService by LazyExtractor(lyricsThemeService)

    private var lyricsModel: LyricsModel? = null
    var scroll: Float = 0.toFloat()
        private set
    private var startScroll: Float = 0.toFloat()
    private var fontsizeTmp: Float = 0.toFloat()
    private var pointersDst0: Float? = null
    private var fontsize0: Float? = null
    private var lyricsRenderer: LyricsRenderer? = null
    private var startTouchX = 0f
    private var startTouchY = 0f
    private var startTouchTime: Long = 0
    private val bottomMarginCache = SimpleCache { this.windowManagerService.dp2px(EOF_BOTTOM_RESERVE) }
    private val scrollWidthCache = SimpleCache { this.windowManagerService.dp2px(1f) }
    private var lastClickTime: Long? = null
    private var recyclerScrollState = RecyclerView.SCROLL_STATE_IDLE
    var overlayScrollResetter: () -> Unit = {}
    var overlayRecyclerView: RecyclerView? = null

    companion object {
        const val EOF_BOTTOM_RESERVE = 60f // padding bottom [dp]
        const val LINEHEIGHT_SCALE_FACTOR = 1.02f
        const val FONTSIZE_SCALE_FACTOR = 0.6f
        const val DOUBLE_CLICK_INTERVAL: Long = 500 // [ms]
    }

    private val fontsizePx: Float
        get() = windowManagerService.dp2px(this.fontsizeTmp)

    val lineheightPx: Float
        get() = fontsizePx * LINEHEIGHT_SCALE_FACTOR

    internal// no scrolling possibility
    val maxScroll: Float
        get() {
            val bottomY = textBottomY
            val reserve = bottomMarginCache.get()
            return if (bottomY > h) {
                bottomY + reserve - h
            } else {
                0f
            }
        }

    private val textBottomY: Float
        get() {
            if (lyricsModel == null)
                return 0f
            val lines = lyricsModel!!.lines
            if (lines.isEmpty())
                return 0f
            val lineheight = lineheightPx
            return lines.size * lineheight + lineheight
        }

    val scrollWidth: Float
        get() = scrollWidthCache.get()

    override fun reset() {
        super.reset()
        scroll = 0f
        startScroll = 0f
        pointersDst0 = null
        fontsize0 = null
        lyricsModel = null
    }

    override fun init() {
        songPreviewController.onGraphicsInitializedEvent(w, paint)
    }

    override fun onRepaint() {
        drawBackground()

        if (this.lyricsRenderer != null) {
            lyricsRenderer?.drawScrollBar()
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
            MotionEvent.ACTION_POINTER_DOWN -> onTouchPointerDown(event)
            MotionEvent.ACTION_POINTER_UP -> onTouchPointerUp(event)
        }
        return false
    }

    private fun onTouchDown(event: MotionEvent) {
        startTouchX = event.x
        startTouchY = event.y
        startTouchTime = System.currentTimeMillis()
        startScroll = scroll
        pointersDst0 = null
    }

    private fun onTouchMove(event: MotionEvent) {
        if (event.pointerCount >= 2) {
            // pinch to font scaling
            if (pointersDst0 != null) {
                val pointersDst1 = hypot((event.getX(1) - event.getX(0)).toDouble(), (event.getY(1) - event
                        .getY(0)).toDouble()).toFloat()
                val scale = (pointersDst1 / pointersDst0!! - 1) * FONTSIZE_SCALE_FACTOR + 1
                val fontsize1 = fontsize0!! * scale
                scroll = startScroll * scale
                previewFontsize(fontsize1)
            }
        }
    }

    private fun onTouchPointerDown(event: MotionEvent) {
        pointersDst0 = hypot((event.getX(1) - event.getX(0)).toDouble(), (event.getY(1) - event.getY(0)).toDouble()).toFloat()
        fontsize0 = fontsizeTmp
        startScroll = scroll
    }

    private fun onTouchPointerUp(event: MotionEvent) {
        pointersDst0 = null // reset initial length
        startScroll = scroll
        // leave a pointer which is still active
        var pointerIndex: Int? = 0
        if (event.pointerCount >= 2) {
            for (i in 0 until event.pointerCount) {
                if (i != event.actionIndex) {
                    pointerIndex = i
                    break
                }
            }
        }
        startTouchY = event.getY(pointerIndex!!)

        songPreviewController.onFontsizeChangedEvent(fontsizeTmp)
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
        this.lyricsRenderer = LyricsRenderer(
                this,
                lyricsModel,
                lyricsThemeService.fontTypeface,
                lyricsThemeService.colorScheme,
                lyricsThemeService.displayStyle
        )
        repaint()
    }

    fun setFontSizes(fontsizeDp: Float) {
        this.fontsizeTmp = fontsizeDp
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
     * @return
     */
    fun scrollByLines(lineheightPart: Float): Boolean {
        return scrollByPx(lineheightPart * lineheightPx)
    }

    fun changedRecyclerScrollState(recyclerScrollState: Int) {
        this.recyclerScrollState = recyclerScrollState
        scrollByPx(0f)
    }

    fun scrollByPx(px: Float): Boolean {
        val maxAbroad = lineheightPx * 6
        val stopRunawayScrollingMargin = lineheightPx * 5
        val boundaryHisteresis = 0.5f
        when {
            scroll < 0 -> {
                val exceeds = -scroll
                when {
                    autoscroll.isRunning -> {
                        scroll = 0f
                    }

                    px < 0 && recyclerScrollState == RecyclerView.SCROLL_STATE_DRAGGING -> {
                        if (exceeds < maxAbroad) {
                            scroll += px * (1f - 1f / maxAbroad * exceeds)
                        }
                    }
                    px < 0 && recyclerScrollState == RecyclerView.SCROLL_STATE_SETTLING -> {
                        if (exceeds > stopRunawayScrollingMargin) {
                            overlayScrollResetter()
                            overlayRecyclerView?.smoothScrollBy(0, exceeds.toInt(), OvershootInterpolator(), 200)
                        } else {
                            scroll += px * (1f - 1f / maxAbroad * exceeds)
                        }
                    }
                    recyclerScrollState == RecyclerView.SCROLL_STATE_IDLE -> {
                        if (scroll < -boundaryHisteresis)
                            overlayRecyclerView?.smoothScrollBy(0, exceeds.toInt(), OvershootInterpolator(), 200)
                    }
                    else -> scroll += px
                }
            }

            scroll > maxScroll -> {
                val exceeds = scroll - maxScroll
                when {
                    px > 0 && recyclerScrollState == RecyclerView.SCROLL_STATE_DRAGGING -> {
                        if (exceeds < maxAbroad) {
                            scroll += px * (1f - 1f / maxAbroad * exceeds)
                        }
                    }
                    px > 0 && recyclerScrollState == RecyclerView.SCROLL_STATE_SETTLING -> {
                        if (exceeds > stopRunawayScrollingMargin) {
                            overlayScrollResetter()
                            overlayRecyclerView?.smoothScrollBy(0, -exceeds.toInt(), OvershootInterpolator(), 200)
                        } else {
                            scroll += px * (1f - 1f / maxAbroad * exceeds)
                        }
                    }
                    recyclerScrollState == RecyclerView.SCROLL_STATE_IDLE -> {
                        if (scroll > maxScroll + boundaryHisteresis)
                            overlayRecyclerView?.smoothScrollBy(0, -exceeds.toInt(), OvershootInterpolator(), 200)
                    }
                    else -> scroll += px
                }
            }

            else -> scroll += px
        }

        val scrollable = when {
            scroll < 0 -> {
                if (autoscroll.isRunning)
                    scroll = 0f
                false
            }
            scroll > maxScroll -> false
            else -> true
        }

        repaint()
        return scrollable
    }

    fun canScrollDown(): Boolean {
        return scroll < maxScroll
    }

    fun onManuallyScrolled(dy: Int) {
        // lines scrolled
        val linePartScrolled = dy.toFloat() / lineheightPx
        // monitor scroll changes\
        if (abs(linePartScrolled) > 0.01f) {
            autoscroll.canvasScrollSubject.onNext(linePartScrolled)
        }
    }

    fun goToBeginning() {
        scroll = 0f
        repaint()
    }
}
