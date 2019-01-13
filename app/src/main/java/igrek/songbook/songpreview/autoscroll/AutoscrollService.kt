package igrek.songbook.songpreview.autoscroll

import android.os.Handler
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.settings.preferences.PreferencesDefinition
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songpreview.renderer.SongPreview
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AutoscrollService {

    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var songPreviewController: Lazy<SongPreviewLayoutController>
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var preferencesService: PreferencesService

    var initialPause: Long = 0 // [ms]
    var autoscrollSpeed: Float = 0.toFloat() // [em / s]
    var autoSpeedAdjustment: Boolean = true
    var dpadKeysSpeedControl: Boolean = true

    private val logger = LoggerFactory.getLogger()
    private var state: AutoscrollState? = null
    private var startTime: Long = 0 // [ms]
    private var scrolledBuffer = 0f

    val canvasScrollSubject = PublishSubject.create<Float>()
    private val aggregatedScrollSubject = PublishSubject.create<Float>()
    val scrollStateSubject = PublishSubject.create<AutoscrollState>()
    val scrollSpeedAdjustmentSubject = PublishSubject.create<Float>()

    private val timerHandler = Handler()
    private val timerRunnable: () -> Unit = {
        if (state != AutoscrollState.OFF) {
            handleAutoscrollStep()
        }
    }

    companion object {
        const val MIN_SPEED = 0.001f // [line / s]
        const val START_NO_WAITING_MIN_SCROLL = 0.9f // [line]
        const val ADJUSTMENT_SPEED_SCALE = 0.0008f // [line / s  /  scrolled lines]
        const val ADJUSTMENT_MAX_SPEED_CHANGE = 0.03f // [line / s  /  scrolled lines]
        const val ADD_INITIAL_PAUSE_SCALE = 180.0f // [ms  /  scrolled lines]
        const val AUTOSCROLL_INTERVAL_TIME = 60f // [ms]
    }

    val isRunning: Boolean
        get() = state == AutoscrollState.WAITING || state == AutoscrollState.SCROLLING

    private val canvas: SongPreview?
        get() = songPreviewController.get().songPreview

    init {
        DaggerIoc.getFactoryComponent().inject(this)
        loadPreferences()
        reset()

        // aggreagate many little scrolls into greater parts (not proper RX method found)
        canvasScrollSubject.observeOn(AndroidSchedulers.mainThread())
                .subscribe { linePartScrolled ->
                    scrolledBuffer += linePartScrolled!!
                    aggregatedScrollSubject.onNext(scrolledBuffer)
                }

        aggregatedScrollSubject.throttleLast(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    onCanvasScrollEvent(scrolledBuffer, canvas!!.scroll)
                    scrolledBuffer = 0f
                }
    }

    private fun loadPreferences() {
        initialPause = preferencesService.getValue(PreferencesDefinition.autoscrollInitialPause, Long::class.java)!!
        autoscrollSpeed = preferencesService.getValue(PreferencesDefinition.autoscrollSpeed, Float::class.java)!!
        autoSpeedAdjustment = preferencesService.getValue(PreferencesDefinition.autoscrollSpeedAutoAdjustment, Boolean::class.java)!!
        dpadKeysSpeedControl = preferencesService.getValue(PreferencesDefinition.autoscrollSpeedDpadKeys, Boolean::class.java)!!
    }

    fun reset() {
        stop()
    }

    fun start() {
        val linePartScroll = canvas!!.scroll / canvas!!.lineheightPx
        if (linePartScroll <= START_NO_WAITING_MIN_SCROLL) {
            start(true)
        } else {
            start(false)
        }
    }

    private fun start(withWaiting: Boolean) {
        if (isRunning) {
            stop()
        }
        if (canvas!!.canScrollDown()) {
            state = if (withWaiting) {
                AutoscrollState.WAITING
            } else {
                AutoscrollState.SCROLLING
            }
            startTime = System.currentTimeMillis()
            timerHandler.postDelayed(timerRunnable, 0)
        }
        scrollStateSubject.onNext(state!!)
    }

    fun stop() {
        state = AutoscrollState.OFF
        timerHandler.removeCallbacks(timerRunnable)
        scrollStateSubject.onNext(state!!)
    }

    private fun handleAutoscrollStep() {
        if (state == AutoscrollState.WAITING) {
            val remainingTimeMs = initialPause + startTime - System.currentTimeMillis()
            if (remainingTimeMs <= 0) {
                state = AutoscrollState.SCROLLING
                timerHandler.postDelayed(timerRunnable, 0)
                onAutoscrollStartedEvent()
            } else {
                val delay = if (remainingTimeMs > 1000) 1000 else remainingTimeMs // cut off over 1000
                timerHandler.postDelayed(timerRunnable, delay)
                onAutoscrollRemainingWaitTimeEvent(remainingTimeMs)
            }
        } else if (state == AutoscrollState.SCROLLING) {
            // em = speed * time
            val lineheightPart = autoscrollSpeed * AUTOSCROLL_INTERVAL_TIME / 1000
            if (canvas!!.scrollByLines(lineheightPart)) {
                // scroll once again later
                timerHandler.postDelayed(timerRunnable, AUTOSCROLL_INTERVAL_TIME.toLong())
            } else {
                // scroll has come to an end
                stop()
                onAutoscrollEndedEvent()
            }
        }
    }

    /**
     * @param dScroll line Part Scrolled
     * @param scroll  current scroll
     */
    private fun onCanvasScrollEvent(dScroll: Float, scroll: Float) {
        if (state == AutoscrollState.WAITING) {
            if (dScroll > 0) { // skip counting down immediately
                skipInitialPause()
            } else if (dScroll < 0) { // increase inital waitng time
                startTime -= (dScroll * ADD_INITIAL_PAUSE_SCALE).toLong()
                val remainingTimeMs = initialPause + startTime - System.currentTimeMillis()
                onAutoscrollRemainingWaitTimeEvent(remainingTimeMs)
            }
        } else if (state == AutoscrollState.SCROLLING) {
            if (dScroll > 0) { // speed up scrolling

                autoscrollSpeed += cutOffMax(dScroll * ADJUSTMENT_SPEED_SCALE, ADJUSTMENT_MAX_SPEED_CHANGE)

            } else if (dScroll < 0) {
                if (scroll <= 0) { // scrolling up to the beginning
                    // set counting down state with additional time
                    state = AutoscrollState.WAITING
                    startTime = System.currentTimeMillis() - initialPause - (dScroll * ADD_INITIAL_PAUSE_SCALE).toLong()
                    val remainingTimeMs = initialPause + startTime - System.currentTimeMillis()
                    onAutoscrollRemainingWaitTimeEvent(remainingTimeMs)
                    return
                } else {
                    // slow down scrolling
                    val dScrollAbs = -dScroll
                    autoscrollSpeed -= cutOffMax(dScrollAbs * ADJUSTMENT_SPEED_SCALE, ADJUSTMENT_MAX_SPEED_CHANGE)
                }
            }
            if (autoscrollSpeed < MIN_SPEED)
                autoscrollSpeed = MIN_SPEED

            scrollSpeedAdjustmentSubject.onNext(autoscrollSpeed)
            logger.info("autoscroll speed adjustment: $autoscrollSpeed line / s")
        }
    }

    private fun cutOffMax(value: Float, max: Float): Float {
        return if (value > max) max else value
    }

    private fun onAutoscrollRemainingWaitTimeEvent(ms: Long) {
        val seconds = java.lang.Long.toString((ms + 500) / 1000)
        val info = uiResourceService.resString(R.string.autoscroll_starts_in, seconds)
        uiInfoService.showInfoWithAction(info, R.string.action_start_now_autoscroll) { this.skipInitialPause() }
    }

    private fun skipInitialPause() {
        state = AutoscrollState.SCROLLING
        uiInfoService.clearSnackBars()
        onAutoscrollStartedEvent()
    }

    private fun onAutoscrollStartUIEvent() {
        if (!isRunning) {
            if (canvas!!.canScrollDown()) {
                start()
                uiInfoService.showInfoWithAction(R.string.autoscroll_started, R.string.action_stop_autoscroll) { this.stop() }
            } else {
                uiInfoService.showInfo(uiResourceService.resString(R.string.end_of_song_autoscroll_stopped))
            }
        } else {
            onAutoscrollStopUIEvent()
        }
    }

    private fun onAutoscrollStartedEvent() {
        uiInfoService.showInfoWithAction(R.string.autoscroll_started, R.string.action_stop_autoscroll) { this.stop() }
    }

    private fun onAutoscrollEndedEvent() {
        uiInfoService.showInfo(uiResourceService.resString(R.string.end_of_song_autoscroll_stopped))
    }

    fun onAutoscrollStopUIEvent() {
        if (isRunning) {
            stop()
            uiInfoService.showInfo(R.string.autoscroll_stopped)
        }
    }

    fun onAutoscrollToggleUIEvent() {
        if (isRunning) {
            onAutoscrollStopUIEvent()
        } else {
            onAutoscrollStartUIEvent()
        }
    }
}
