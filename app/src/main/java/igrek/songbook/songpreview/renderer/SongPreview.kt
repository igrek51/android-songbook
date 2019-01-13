package igrek.songbook.songpreview.renderer

import android.content.Context
import android.view.MotionEvent
import android.view.View
import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.model.lyrics.LyricsModel
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.songpreview.quickmenu.QuickMenuAutoscroll
import igrek.songbook.songpreview.quickmenu.QuickMenuTranspose
import igrek.songbook.songpreview.renderer.canvas.BaseCanvasView
import igrek.songbook.songpreview.theme.LyricsThemeService
import igrek.songbook.system.WindowManagerService
import igrek.songbook.system.cache.SimpleCache
import javax.inject.Inject

class SongPreview(context: Context) : BaseCanvasView(context), View.OnTouchListener {

    @Inject
    lateinit var songPreviewController: Lazy<SongPreviewLayoutController>
    @Inject
    lateinit var autoscroll: Lazy<AutoscrollService>
    @Inject
    lateinit var quickMenuTranspose: Lazy<QuickMenuTranspose>
    @Inject
    lateinit var quickMenuAutoscroll: Lazy<QuickMenuAutoscroll>
    @Inject
    lateinit var windowManagerService: WindowManagerService
    @Inject
    lateinit var lyricsThemeService: LyricsThemeService

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
    private val bottomMarginCache = SimpleCache { windowManagerService.dp2px(EOF_BOTTOM_RESERVE) }
    private val scrollWidthCache = SimpleCache { windowManagerService.dp2px(1f) }
    private var lastClickTime: Long? = null

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
            if (lines == null || lines.isEmpty())
                return 0f
            val lastLine = lines[lines.size - 1] ?: return 0f
            val lineheight = lineheightPx
            return lastLine.y * lineheight + lineheight
        }

    val scrollWidth: Float
        get() = scrollWidthCache.get()

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    override fun reset() {
        super.reset()
        scroll = 0f
        startScroll = 0f
        pointersDst0 = null
        fontsize0 = null
        lyricsModel = null
    }

    override fun init() {
        songPreviewController.get().onGraphicsInitializedEvent(w, h, paint)
    }

    override fun onRepaint() {
        // draw Background
        setColor(0x000000)
        clearScreen()

        if (this.lyricsRenderer != null) {
            lyricsRenderer!!.drawScrollBar()
            lyricsRenderer!!.drawFileContent(fontsizePx, lineheightPx)
        }

        drawQuickMenuOverlay()
    }

    private fun drawQuickMenuOverlay() {
        if (quickMenuTranspose.get().isVisible) {
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
                val pointersDst1 = Math.hypot((event.getX(1) - event.getX(0)).toDouble(), (event.getY(1) - event
                        .getY(0)).toDouble()).toFloat()
                val scale = (pointersDst1 / pointersDst0!! - 1) * FONTSIZE_SCALE_FACTOR + 1
                val fontsize1 = fontsize0!! * scale
                scroll = startScroll * scale
                previewFontsize(fontsize1)
            }
        }
    }

    private fun onTouchPointerDown(event: MotionEvent) {
        pointersDst0 = Math.hypot((event.getX(1) - event.getX(0)).toDouble(), (event.getY(1) - event.getY(0)).toDouble()).toFloat()
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

        songPreviewController.get().onFontsizeChangedEvent(fontsizeTmp)
    }

    fun onClick() {
        val now = System.currentTimeMillis()
        if (songPreviewController.get().isQuickMenuVisible) {
            quickMenuTranspose.get().isVisible = false
            quickMenuAutoscroll.get().isVisible = false
            repaint()
        } else {
            if (autoscroll.get().isRunning) {
                autoscroll.get().onAutoscrollStopUIEvent()
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
        autoscroll.get().onAutoscrollToggleUIEvent()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isInitialized) {
            songPreviewController.get().onPreviewSizeChange(w, h)
        }
    }

    fun setCRDModel(lyricsModel: LyricsModel?) {
        this.lyricsModel = lyricsModel
        this.lyricsRenderer = LyricsRenderer(this, lyricsModel, lyricsThemeService.fontTypeface!!)
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

    fun scrollByPx(px: Float): Boolean {
        scroll += px
        var scrollable = true
        val maxScroll = maxScroll
        // cut off
        if (scroll < 0) {
            scroll = 0f
            scrollable = false
        }
        if (scroll > maxScroll) {
            scroll = maxScroll
            scrollable = false
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
        if (Math.abs(linePartScrolled) > 0.01f) {
            autoscroll.get().canvasScrollSubject.onNext(linePartScrolled)
        }
    }

    fun goToBeginning() {
        scroll = 0f
        repaint()
    }
}
