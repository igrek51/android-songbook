package igrek.songbook.songpreview.renderer

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.recyclerview.widget.RecyclerView
import igrek.songbook.cast.SongCastService
import igrek.songbook.chords.loader.LyricsLoader
import igrek.songbook.chords.model.LyricsModel
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.playlist.PlaylistService
import igrek.songbook.settings.chordsnotation.ChordsNotation
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.settings.theme.LyricsThemeService
import igrek.songbook.songpreview.quickmenu.QuickMenuAutoscroll
import igrek.songbook.songpreview.quickmenu.QuickMenuCast
import igrek.songbook.songpreview.quickmenu.QuickMenuTranspose
import igrek.songbook.songpreview.renderer.canvas.CanvasView
import igrek.songbook.songpreview.scroll.AutoscrollService
import igrek.songbook.system.WindowManagerService
import igrek.songbook.util.applyMin
import igrek.songbook.util.lookup.SimpleCache
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot


class SongPreview(
    context: Context,
    val onInit: () -> Unit,
    val onFontsizeChanged: (fontsize: Float) -> Unit,
    val onPreviewSizeChanged: () -> Unit,
    autoscrollService: LazyInject<AutoscrollService> = appFactory.autoscrollService,
    quickMenuTranspose: LazyInject<QuickMenuTranspose> = appFactory.quickMenuTranspose,
    quickMenuAutoscroll: LazyInject<QuickMenuAutoscroll> = appFactory.quickMenuAutoscroll,
    quickMenuCast: LazyInject<QuickMenuCast> = appFactory.quickMenuCast,
    windowManagerService: LazyInject<WindowManagerService> = appFactory.windowManagerService,
    lyricsThemeService: LazyInject<LyricsThemeService> = appFactory.lyricsThemeService,
    playlistService: LazyInject<PlaylistService> = appFactory.playlistService,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
    songCastService: LazyInject<SongCastService> = appFactory.songCastService,
    lyricsLoader: LazyInject<LyricsLoader> = appFactory.lyricsLoader,
) : View.OnTouchListener {
    private val autoscroll by LazyExtractor(autoscrollService)
    private val quickMenuTranspose by LazyExtractor(quickMenuTranspose)
    private val quickMenuAutoscroll by LazyExtractor(quickMenuAutoscroll)
    private val quickMenuCast by LazyExtractor(quickMenuCast)
    private val windowManagerService by LazyExtractor(windowManagerService)
    private val lyricsThemeService by LazyExtractor(lyricsThemeService)
    private val playlistService by LazyExtractor(playlistService)
    private val preferencesState by LazyExtractor(preferencesState)
    private val songCastService by LazyExtractor(songCastService)
    private val lyricsLoader by LazyExtractor(lyricsLoader)

    val canvas: CanvasView = CanvasView(context, onInit, ::onRepaint, onPreviewSizeChanged)
    private var lyricsRenderer: LyricsRenderer = LyricsRenderer(this, canvas, preferencesState.get())
    var lyricsModel: LyricsModel = LyricsModel()
        private set
    var scroll: Float = 0f
        private set
    var scrollX: Float = 0f
        private set
    private var startScroll: Float = 0f
    private var startScrollX: Float = 0f
    private var fontsizeTmp: Float = 0f
    private var pointersDst0: Float? = null
    private var fontsize0: Float? = null
    private var startTouchY = 0f
    private var startTouchX = 0f
    private var startTouchScrollX = 0f
    private var startTouchTime: Long = 0
    private var multiTouch = false
    private val bottomMarginCache = SimpleCache {
        this.windowManagerService.dp2px(EOF_BOTTOM_RESERVE)
    }
    private val scrollThicknessCache = SimpleCache { this.windowManagerService.dp2px(4f) }
    val densityPixelSize = SimpleCache { this.windowManagerService.dp2px(1f) }
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

    val w: Int get() = canvas.w
    val h: Int get() = canvas.h
    private val fontsizePx: Float get() = windowManagerService.dp2px(this.fontsizeTmp)
    val lineheightPx: Float get() = fontsizePx * LINEHEIGHT_SCALE_FACTOR

    internal val maxScroll: Float get() {
        val bottomY = textBottomY.get()
        val reserve = bottomMarginCache.get()
        return when {
            isCastPresentingSlides -> bottomY
            bottomY > canvas.h -> bottomY + reserve - canvas.h
            else -> 0f
        }
    }

    internal val maxScrollX: Float get() {
        return when (textRightX.get() > canvas.w) {
            true -> textRightX.get() - canvas.w
            false -> 0f
        }
    }

    private val textBottomY: SimpleCache<Float> = SimpleCache {
        val lines = lyricsModel.lines
        if (lines.isEmpty())
            return@SimpleCache 0f
        (lines.size + 1) * lineheightPx
    }

    val visualLinesAtEnd: Float get() {
        val bottom = textBottomY.get()
        return when (bottom < canvas.h) {
            true -> bottom / lineheightPx
            false -> canvas.h / lineheightPx
        }
    }

    val allLinesEm: Float get() = textBottomY.get() / lineheightPx
    val scrollEm: Float get() = when {
        lineheightPx == 0f -> 0f
        else -> scroll / lineheightPx
    }
    val lastVisibleLine: Float get() = scrollEm + visualLinesAtEnd
    private val textRightX: SimpleCache<Float> = SimpleCache {
        (lyricsModel.lines.maxOfOrNull { it.maxRightX } ?: 0f) * fontsizePx
    }
    val scrollThickness: Float get() = scrollThicknessCache.get()
    val eyeFocusLines: Float get() = autoscroll.eyeFocus

    private val isQuickMenuVisible: Boolean get() =
        quickMenuTranspose.isVisible || quickMenuAutoscroll.isVisible || quickMenuCast.isVisible

    val isCastPresentingSlides: Boolean
        get() = songCastService.isPresenting() && preferencesState.castScrollControl.slideLines > 0
    private var slideCurrentIndex: Int = -1
    private var slideTargetIndex: Int = -1
    var slideCurrentModel: LyricsModel = LyricsModel()
    var slideTargetModel: LyricsModel = LyricsModel()
    private var slideAnimationProgress: Float = 1f
    private val isSlidesMode: Boolean get() = slideTargetIndex >= 0
    var castSlideMarkedLinesIndices: MutableList<Int> = mutableListOf()
    private var slideAnimationJob: Job? = null

    fun reset() {
        canvas.reset()
        scroll = 0f
        scrollX = 0f
        startScroll = 0f
        startScrollX = 0f
        pointersDst0 = null
        fontsize0 = null
        lyricsModel = LyricsModel()
        textBottomY.invalidate()
        textRightX.invalidate()
        slideCurrentIndex = -1
        slideTargetIndex = -1
        slideAnimationProgress = 1f
        castSlideMarkedLinesIndices.clear()
        slideCurrentModel = LyricsModel()
        slideTargetModel = LyricsModel()
    }

    private fun onRepaint() {
        when (isSlidesMode) {
            true -> lyricsRenderer.drawSlides(
                slideCurrentIndex, slideTargetIndex,
                slideAnimationProgress,
                lineheightPx, fontsizePx,
            )
            else -> lyricsRenderer.drawAllLyrics(
                lineheightPx,
                fontsizePx,
                lyricsModel,
                quickMenuVisible = quickMenuTranspose.isVisible || quickMenuAutoscroll.isVisible || quickMenuCast.isVisible,
            )
        }
    }

    fun setLyricsModel(lyricsModel: LyricsModel) {
        this.lyricsModel = lyricsModel
        textBottomY.invalidate()
        textRightX.invalidate()
        lyricsRenderer = LyricsRenderer(this, canvas, preferencesState)
        canvas.repaint()
    }

    fun setFontSizes(fontsizeDp: Float) {
        this.fontsizeTmp = fontsizeDp
        textBottomY.invalidate()
        textRightX.invalidate()
    }

    private fun previewFontsize(fontsize1: Float) {
        val minScreen = if (canvas.w > canvas.h) canvas.h else canvas.w
        if (fontsize1 >= 5 && fontsize1 <= minScreen / 5) {
            setFontSizes(fontsize1)
            canvas.repaint()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
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
                        (event.getX(1) - event.getX(0)).toDouble(),
                        (event.getY(1) - event.getY(0)).toDouble()
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

        onFontsizeChanged(fontsizeTmp)
    }

    private fun onTouchUp(event: MotionEvent) {
        if (!lyricsThemeService.horizontalScroll) {
            if (!multiTouch) {
                val dx = event.x - startTouchX
                val adx = abs(dx)
                if (adx > GESTURE_HORIZONTAL_SWIPE * canvas.w) {
                    if (dx > 0) {
                        playlistService.goToNextOrPrevious(-1)
                    } else {
                        playlistService.goToNextOrPrevious(+1)
                    }
                }
            }
        }
    }

    fun onClick() {
        val now = System.currentTimeMillis()
        if (isQuickMenuVisible) {
            quickMenuTranspose.isVisible = false
            quickMenuAutoscroll.isVisible = false
            quickMenuCast.isVisible = false
            canvas.repaint()
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
                    autoscroll.isRunning -> scroll = 0f
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
            else -> when {
                autoscroll.isWaiting && py > 0 -> {}
                else -> scroll += py
            }
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
        appFactory.scrollService.g.reportSongScrolled(py / lineheightPx)
        canvas.repaint()
        return scrollable
    }

    private fun scrollByPxHorizontal(px: Float) {
        if (!lyricsThemeService.horizontalScroll) {
            scrollX = 0f
            return
        }
        scrollX += px
        when {
            scrollX < 0f -> scrollX = 0f
            scrollX > maxScrollX -> scrollX = maxScrollX
        }
        canvas.repaint()
    }

    fun canScrollDown(): Boolean = maxScroll > 0 && scroll < maxScroll

    fun onManuallyScrolled(dy: Float) {
        val linePartScrolled = dy / lineheightPx
        if (abs(linePartScrolled) > 0.01f) {
            autoscroll.canvasScrollSubject.onNext(linePartScrolled)
        }
    }

    fun goToBeginning() {
        scroll = 0f
        scrollX = 0f
        canvas.repaint()
    }

    fun showSlide(slideIndex: Int, slideText: String, srcNotation: ChordsNotation) {
        if (this.slideTargetIndex == -1) {
            this.slideCurrentIndex = slideIndex
            this.slideTargetIndex = slideIndex
            this.slideTargetModel = lyricsLoader.loadEphemeralLyrics(slideText, w, srcNotation)
            this.slideAnimationProgress = 1f
        } else {
            this.slideCurrentIndex = this.slideTargetIndex
            this.slideCurrentModel = this.slideTargetModel
            this.slideTargetIndex = slideIndex
            this.slideTargetModel = lyricsLoader.loadEphemeralLyrics(slideText, w, srcNotation)
            this.slideAnimationProgress = 0f
        }
        slideAnimationJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }
        this.slideAnimationJob = GlobalScope.launch (Dispatchers.Main) {
            try {
                canvas.repaint()
                val animationTime = 300L
                val animationSteps = 10
                while (slideAnimationProgress < 1f) {
                    delay(animationTime / animationSteps)
                    slideAnimationProgress += 1f / animationSteps
                    canvas.repaint()
                }
            } catch (_: CancellationException) {}
        }
    }

    fun evaluateCastSlideZone() {
        val visualLinesCount = preferencesState.castScrollControl.slideLines
        val wholeLinesSkipped = (scroll / lineheightPx).applyMin(0f).toInt()

        val focusedLine = lyricsModel.lines.getOrNull(wholeLinesSkipped)
            ?: run {
                castSlideMarkedLinesIndices.clear()
                return
            }

        val primalIndexStart: Int = focusedLine.primalIndex
        val primalIndexEnd: Int = primalIndexStart + visualLinesCount - 1

        castSlideMarkedLinesIndices.clear()
        for (primalIndex in primalIndexStart .. primalIndexEnd) {
            lyricsModel.lines.indexOfFirst { it.primalIndex == primalIndex }
                .takeIf { it >= 0 }
                ?.let {
                    castSlideMarkedLinesIndices.add(it)
                }
        }
        val enclosingLineIndex = lyricsModel.lines.indexOfLast { it.primalIndex == primalIndexEnd }
            .takeIf { it >= 0 } ?: (lyricsModel.lines.size - 1)
        castSlideMarkedLinesIndices.add(enclosingLineIndex)
    }
}