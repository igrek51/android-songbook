package igrek.songbook.songpreview.autoscroll

import android.os.Handler
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.playlist.PlaylistLayoutController
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songpreview.renderer.SongPreview
import igrek.songbook.util.limitTo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class AutoscrollService(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
    playlistLayoutController: LazyInject<PlaylistLayoutController> = appFactory.playlistLayoutController,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songPreviewController by LazyExtractor(songPreviewLayoutController)
    private val playlistLayoutController by LazyExtractor(playlistLayoutController)
    private val preferencesState by LazyExtractor(preferencesState)

    var initialPause: Long // [ms]
        get() = preferencesState.autoscrollInitialPause
        set(value) {
            preferencesState.autoscrollInitialPause = value
        }
    var autoscrollSpeed: Float // [em / s]
        get() = preferencesState.autoscrollSpeed
        set(value) {
            preferencesState.autoscrollSpeed = value
        }
    private var autoSpeedAdjustment: Boolean
        get() = preferencesState.autoscrollSpeedAutoAdjustment
        set(value) {
            preferencesState.autoscrollSpeedAutoAdjustment = value
        }
    private var volumeKeysSpeedControl: Boolean
        get() = preferencesState.autoscrollSpeedVolumeKeys
        set(value) {
            preferencesState.autoscrollSpeedVolumeKeys = value
        }

    private val logger = LoggerFactory.logger
    private var state: AutoscrollState = AutoscrollState.OFF
    private var startTime: Long = 0 // [ms]
    private var nextSongAtTime: Long = 0 // [ms]
    private var scrolledBuffer = 0f
    private var startAutoscrollOnNextSong = false

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
        const val MAX_SPEED = 1.000f // [line / s]
        const val START_NO_WAITING_MIN_SCROLL = 0.9f // [line]
        const val ADJUSTMENT_SPEED_SCALE = 0.001f // [line / s  /  scrolled lines]
        const val ADJUSTMENT_MAX_SPEED_CHANGE = 0.03f // [line / s  /  scrolled lines]
        const val ADD_INITIAL_PAUSE_SCALE = 180.0f // [ms  /  scrolled lines]
        const val AUTOSCROLL_INTERVAL_TIME = 60f // [ms]
        const val VOLUME_BTNS_SPEED_STEP = 0.020f // [line / s]
        const val MIN_NEXT_SONG_TIME: Long = 3000 // [ms]
    }

    val isRunning: Boolean
        get() = when (state) {
            AutoscrollState.WAITING, AutoscrollState.SCROLLING, AutoscrollState.NEXT_SONG_COUNTDOWN -> true
            AutoscrollState.OFF -> false
        }

    private val songPreview: SongPreview?
        get() = songPreviewController.songPreview

    init {
        // aggreagate many little scrolls into greater parts (not proper RX method found)
        canvasScrollSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ linePartScrolled ->
                    scrolledBuffer += linePartScrolled!!
                    aggregatedScrollSubject.onNext(scrolledBuffer)
                }, UiErrorHandler::handleError)

        aggregatedScrollSubject
                .throttleLast(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (songPreview != null)
                        onCanvasScrollEvent(scrolledBuffer, songPreview?.scroll ?: 0f)
                    scrolledBuffer = 0f
                }, UiErrorHandler::handleError)
    }

    fun reset() {
        stop()
    }

    fun start() {
        songPreview?.run {
            val linePartScroll = scroll / lineheightPx
            if (linePartScroll <= START_NO_WAITING_MIN_SCROLL) {
                start(true)
            } else {
                start(false)
            }
        }
    }

    private fun start(withWaiting: Boolean) {
        if (isRunning) {
            stop()
        }
        if (songPreview?.canScrollDown() == true) {
            state = if (withWaiting) {
                AutoscrollState.WAITING
            } else {
                AutoscrollState.SCROLLING
            }
            startTime = System.currentTimeMillis()
            timerHandler.postDelayed(timerRunnable, 0)
        } else if (canCountdownToNextSong()) {
            countdownToNextSong()
        }
        scrollStateSubject.onNext(state)
    }

    fun stop() {
        state = AutoscrollState.OFF
        timerHandler.removeCallbacks(timerRunnable)
        scrollStateSubject.onNext(state)
    }

    private fun autostart() {
        if (songPreview?.canScrollDown() == true) {
            start()
        }
    }

    fun onLoad() {
        if (preferencesState.autoscrollAutostart) {
            autostart()
        } else if (startAutoscrollOnNextSong) {
            startAutoscrollOnNextSong = false
            start()
        }
    }

    private fun handleAutoscrollStep() {
        when (state) {
            AutoscrollState.WAITING -> {
                val remainingTimeMs = initialPause + startTime - System.currentTimeMillis()
                if (remainingTimeMs <= 0) {
                    state = AutoscrollState.SCROLLING
                    timerHandler.postDelayed(timerRunnable, 0)
                    onAutoscrollStartedEvent()
                } else {
                    val delay = remainingTimeMs.limitTo(1000)
                    timerHandler.postDelayed(timerRunnable, delay)
                    onAutoscrollRemainingWaitTimeEvent(remainingTimeMs)
                }
            }
            AutoscrollState.SCROLLING -> {
                // em = speed * time
                val lineheightPart = autoscrollSpeed * AUTOSCROLL_INTERVAL_TIME / 1000
                if (songPreview?.scrollByLines(lineheightPart) != false) {
                    // scroll once again later
                    timerHandler.postDelayed(timerRunnable, AUTOSCROLL_INTERVAL_TIME.toLong())
                } else {
                    // scroll has come to an end
                    stop()
                    onAutoscrollEndedEvent()
                }
            }
            AutoscrollState.NEXT_SONG_COUNTDOWN -> {
                val remainingTimeMs = nextSongAtTime - System.currentTimeMillis()
                if (remainingTimeMs <= 0) {
                    state = AutoscrollState.OFF
                    // Turn on autoscroll again in next song
                    startAutoscrollOnNextSong = true
                    playlistLayoutController.goToNextOrPrevious(+1)
                } else {
                    val delay = remainingTimeMs.limitTo(1000)
                    onCountdownToNextSongRemainingTime(remainingTimeMs)
                    timerHandler.postDelayed(timerRunnable, delay)
                }
            }
        }
    }

    /**
     * @param dScroll line Part Scrolled
     * @param scroll  current scroll
     */
    private fun onCanvasScrollEvent(dScroll: Float, scroll: Float) {
        when (state) {
            AutoscrollState.WAITING -> {
                if (dScroll > 0) { // skip counting down immediately
                    skipInitialPause()
                } else if (dScroll < 0) { // increase inital waitng time
                    startTime -= (dScroll * ADD_INITIAL_PAUSE_SCALE).toLong()
                    val remainingTimeMs = initialPause + startTime - System.currentTimeMillis()
                    onAutoscrollRemainingWaitTimeEvent(remainingTimeMs)
                }
            }
            AutoscrollState.SCROLLING -> {
                if (dScroll > 0) {
                    // speed up scrolling
                    val delta =
                        +cutOffMax(dScroll * ADJUSTMENT_SPEED_SCALE, ADJUSTMENT_MAX_SPEED_CHANGE)
                    autoAdjustScrollSpeed(delta)

                } else if (dScroll < 0) {
                    if (scroll <= 0) { // scrolling up to the beginning
                        // set counting down state with additional time
                        state = AutoscrollState.WAITING
                        startTime =
                            System.currentTimeMillis() - initialPause - (dScroll * ADD_INITIAL_PAUSE_SCALE).toLong()
                        val remainingTimeMs = initialPause + startTime - System.currentTimeMillis()
                        onAutoscrollRemainingWaitTimeEvent(remainingTimeMs)
                        return
                    } else {
                        // slow down scrolling
                        val delta = -cutOffMax(
                            -dScroll * ADJUSTMENT_SPEED_SCALE,
                            ADJUSTMENT_MAX_SPEED_CHANGE
                        )
                        autoAdjustScrollSpeed(delta)
                    }
                }
            }
            AutoscrollState.NEXT_SONG_COUNTDOWN -> {
                if (dScroll < 0) { // scroll up
                    onAutoscrollStopUIEvent()
                }
            }
        }
    }

    private fun autoAdjustScrollSpeed(delta: Float) {
        if (autoSpeedAdjustment) {
            addAutoscrollSpeed(delta)
            logger.info("autoscroll speed adjusted: $autoscrollSpeed line / s")
        }
    }

    private fun cutOffMax(value: Float, max: Float): Float {
        return if (value > max) max else value
    }

    private fun onAutoscrollRemainingWaitTimeEvent(ms: Long) {
        val seconds = ((ms + 500) / 1000).toString()
        uiInfoService.showInfoAction(R.string.autoscroll_starts_in, seconds, actionResId = R.string.action_start_now_autoscroll) {
            this.skipInitialPause()
        }
    }

    private fun skipInitialPause() {
        state = AutoscrollState.SCROLLING
        uiInfoService.clearSnackBars()
        onAutoscrollStartedEvent()
    }

    private fun onAutoscrollStartUIEvent() {
        if (!isRunning && songPreview != null) {
            when {
                songPreview?.canScrollDown() == true -> {
                    start()
                    uiInfoService.showInfoAction(
                        R.string.autoscroll_started,
                        actionResId = R.string.action_stop_autoscroll
                    ) {
                        this.stop()
                    }
                }
                canCountdownToNextSong() -> {
                    start()
                }
                else -> {
                    uiInfoService.showInfo(R.string.end_of_song_autoscroll_stopped)
                }
            }
        }
    }

    private fun onAutoscrollStartedEvent() {
        uiInfoService.showInfoAction(
            R.string.autoscroll_started,
            actionResId = R.string.action_stop_autoscroll
        ) {
            this.stop()
        }
    }

    private fun onAutoscrollEndedEvent() {
        if (canCountdownToNextSong()) {
            countdownToNextSong()
        } else {
            uiInfoService.showInfo(R.string.end_of_song_autoscroll_stopped)
        }
    }

    private fun canCountdownToNextSong(): Boolean {
        return preferencesState.autoscrollForwardNextSong && playlistLayoutController.hasNextSong()
    }

    private fun countdownToNextSong() {
        state = AutoscrollState.NEXT_SONG_COUNTDOWN
        val visibleLinesAtEnd = songPreview?.visibleLinesAtEnd ?: 0f
        val visibleLinesMillis = visibleLinesAtEnd / autoscrollSpeed * 1000
        var waitTimeMs: Long = when {
            songPreview?.scroll ?: 0f <= 0f -> { // at the beginning
                visibleLinesMillis
            }
            else -> {
                visibleLinesMillis * 0.5  // eye focus is in the middle of the screen when autoscroll has ended
            }
        }.toLong()
        if (waitTimeMs < MIN_NEXT_SONG_TIME)
            waitTimeMs = MIN_NEXT_SONG_TIME
        nextSongAtTime = System.currentTimeMillis() + waitTimeMs
        timerHandler.postDelayed(timerRunnable, 0)
        scrollStateSubject.onNext(state)
    }

    private fun onCountdownToNextSongRemainingTime(ms: Long) {
        val seconds = ((ms + 500) / 1000).toString()
        uiInfoService.showInfoAction(
            R.string.autoscroll_forward_next_song_in,
            seconds,
            actionResId = R.string.action_stop_autoscroll
        ) {
            this.stop()
        }
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

    private fun addAutoscrollSpeed(delta: Float) {
        autoscrollSpeed += delta
        if (autoscrollSpeed < MIN_SPEED) autoscrollSpeed = MIN_SPEED
        if (autoscrollSpeed > MAX_SPEED) autoscrollSpeed = MAX_SPEED
        scrollSpeedAdjustmentSubject.onNext(autoscrollSpeed)
    }

    fun onVolumeUp(): Boolean {
        if (!volumeKeysSpeedControl || songPreview == null)
            return false
        when (state) {
            AutoscrollState.OFF -> onAutoscrollStartUIEvent()
            AutoscrollState.WAITING -> skipInitialPause()
            AutoscrollState.SCROLLING -> addAutoscrollSpeed(+VOLUME_BTNS_SPEED_STEP)
            AutoscrollState.NEXT_SONG_COUNTDOWN -> {
            }
        }
        return true
    }

    fun onVolumeDown(): Boolean {
        if (!volumeKeysSpeedControl || songPreview == null)
            return false
        when (state) {
            AutoscrollState.OFF -> {
            }
            AutoscrollState.WAITING -> onAutoscrollStopUIEvent()
            AutoscrollState.SCROLLING -> addAutoscrollSpeed(-VOLUME_BTNS_SPEED_STEP)
            AutoscrollState.NEXT_SONG_COUNTDOWN -> onAutoscrollStopUIEvent()
        }
        return true
    }
}
