package igrek.songbook.songpreview.autoscroll

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.playlist.PlaylistService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songpreview.renderer.SongPreview
import igrek.songbook.util.cutOffMin
import igrek.songbook.util.limitBetween
import igrek.songbook.util.limitTo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class AutoscrollService(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
    playlistService: LazyInject<PlaylistService> = appFactory.playlistService,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songPreviewController by LazyExtractor(songPreviewLayoutController)
    private val playlistService by LazyExtractor(playlistService)
    private val preferencesState by LazyExtractor(preferencesState)
    private val songsRepository by LazyExtractor(songsRepository)

    var autoscrollSpeed: Float // [em / s]
        get() = preferencesState.autoscrollSpeed
        set(value) {
            preferencesState.autoscrollSpeed = value
            persistIndividualSongSpeed()
        }
    var eyeFocus: Float = 0f
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
    val isEyeFocusZoneOn: Boolean
        get() = preferencesState.autoscrollShowEyeFocus

    private val logger = LoggerFactory.logger
    private var state: AutoscrollState = AutoscrollState.OFF
    private var previousStepTime: Long = 0 // [ms]
    private var lastWaitingTimeRefresh: Long = 0 // [ms]
    private var nextSongAtTime: Long = 0 // [ms]
    private var scrolledBuffer = 0f
    private var startAutoscrollOnNextSong = false
    private var currentSongIdentifier: SongIdentifier? = null

    val canvasScrollSubject = PublishSubject.create<Float>()
    private val aggregatedScrollSubject = PublishSubject.create<Float>()
    val scrollStateSubject = PublishSubject.create<AutoscrollState>()
    val scrollSpeedAdjustmentSubject = PublishSubject.create<Float>()

    private val timerHandler = Handler(Looper.getMainLooper())
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
        const val AUTOSCROLL_INTERVAL_TIME = 60L // [ms]
        const val VOLUME_BTNS_SPEED_STEP = 0.020f // [line / s]
        const val MIN_NEXT_SONG_TIME: Long = 3000 // [ms]
        const val WAITING_TIME_ADJUST_MODIFIER = 0.4f // fraction of scrolled pixels
    }

    val isRunning: Boolean
        get() = when (state) {
            AutoscrollState.WAITING, AutoscrollState.SCROLLING, AutoscrollState.ENDING, AutoscrollState.NEXT_SONG_COUNTDOWN -> true
            AutoscrollState.OFF -> false
        }

    val isWaiting: Boolean
        get() = state == AutoscrollState.WAITING

    private val songPreview: SongPreview?
        get() = songPreviewController.songPreview

    init {
        // aggreagate many little scrolls into greater parts (not proper RX method found)
        canvasScrollSubject
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ linePartScrolled ->
                scrolledBuffer += linePartScrolled
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
                start(withWaiting = true)
            } else {
                start(withWaiting = false)
            }
        }
    }

    private fun start(withWaiting: Boolean) {
        if (isRunning) {
            stop()
        }

        songPreview?.let { songPreview ->
            if (songPreview.canScrollDown()) {
                if (withWaiting) {
                    state = AutoscrollState.WAITING
                    eyeFocus = 0f
                    previousStepTime = System.currentTimeMillis()
                } else {
                    state = AutoscrollState.SCROLLING
                    val eyeFocusPx = songPreview.scroll + songPreview.h / 2
                    eyeFocus = eyeFocusPx / songPreview.lineheightPx
                }
                timerHandler.postDelayed(timerRunnable, 0)
            } else if (canCountdownToNextSong()) {
                countdownToNextSong()
            }
            null
        }

        scrollStateSubject.onNext(state)
    }

    fun stop() {
        state = AutoscrollState.OFF
        timerHandler.removeCallbacks(timerRunnable)
        scrollStateSubject.onNext(state)
        eyeFocus = 0f
    }

    private fun autostart() {
        if (songPreview?.canScrollDown() == true) {
            start()
        }
    }

    fun onLoad(songIdentifier: SongIdentifier) {
        if (preferencesState.autoscrollIndividualSpeed) {
            songsRepository.songTweakDao.getSongAutoscrollSpeed(songIdentifier)?.let { songSpeed ->
                preferencesState.autoscrollSpeed = songSpeed
            }
        }
        currentSongIdentifier = songIdentifier

        if (preferencesState.autoscrollAutostart) {
            autostart()
        } else if (startAutoscrollOnNextSong) {
            startAutoscrollOnNextSong = false
            start()
        }
    }

    fun onAutoscrollStopUIEvent() {
        if (isRunning) {
            stop()
            songPreview?.repaint()
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

    fun onVolumeUp(): Boolean {
        if (!volumeKeysSpeedControl || songPreview == null)
            return false
        when (state) {
            AutoscrollState.OFF -> onAutoscrollStartUIEvent()
            AutoscrollState.WAITING -> skipInitialPause()
            AutoscrollState.SCROLLING -> addAutoscrollSpeed(+VOLUME_BTNS_SPEED_STEP)
            AutoscrollState.ENDING -> addAutoscrollSpeed(+VOLUME_BTNS_SPEED_STEP)
            AutoscrollState.NEXT_SONG_COUNTDOWN -> {}
        }
        return true
    }

    fun onVolumeDown(): Boolean {
        if (!volumeKeysSpeedControl || songPreview == null)
            return false
        when (state) {
            AutoscrollState.OFF -> {}
            AutoscrollState.WAITING -> onAutoscrollStopUIEvent()
            AutoscrollState.SCROLLING -> addAutoscrollSpeed(-VOLUME_BTNS_SPEED_STEP)
            AutoscrollState.ENDING -> addAutoscrollSpeed(-VOLUME_BTNS_SPEED_STEP)
            AutoscrollState.NEXT_SONG_COUNTDOWN -> onAutoscrollStopUIEvent()
        }
        return true
    }

    private fun handleAutoscrollStep() {
        songPreview?.let { songPreview ->
            when (state) {

                AutoscrollState.WAITING -> {
                    val remainingWaitingTimeS = remainingWaitingTimeS()

                    val elapsedTimeS =
                        (System.currentTimeMillis() - previousStepTime).toFloat() / 1000f
                    eyeFocus += elapsedTimeS * autoscrollSpeed
                    previousStepTime = System.currentTimeMillis()

                    songPreview.repaint()

                    if (remainingWaitingTimeS <= 0) {
                        state = AutoscrollState.SCROLLING
                        timerHandler.postDelayed(timerRunnable, 0)
                        onAutoscrollStartedEvent()
                    } else {
                        timerHandler.postDelayed(timerRunnable, AUTOSCROLL_INTERVAL_TIME)
                        if (System.currentTimeMillis() - lastWaitingTimeRefresh >= 500) {
                            showAutoscrollWaitingTime(remainingWaitingTimeS)
                            lastWaitingTimeRefresh = System.currentTimeMillis()
                        }
                    }
                }

                AutoscrollState.SCROLLING -> {
                    // em = speed * time
                    val linesEm = autoscrollSpeed * AUTOSCROLL_INTERVAL_TIME / 1000f
                    if (!songPreview.scrollByLines(linesEm)) {
                        // scroll has come to an end (eye focus in the middle)
                        state = AutoscrollState.ENDING
                    }

                    // eye focus always in the middle of the screen
                    eyeFocus = (songPreview.scroll + songPreview.h / 2) / songPreview.lineheightPx

                    timerHandler.postDelayed(timerRunnable, AUTOSCROLL_INTERVAL_TIME)
                }

                AutoscrollState.ENDING -> {
                    val lineheightPart = autoscrollSpeed * AUTOSCROLL_INTERVAL_TIME / 1000
                    eyeFocus += lineheightPart

                    songPreview.repaint()

                    if (eyeFocus <= songPreview.allLines) {
                        timerHandler.postDelayed(timerRunnable, AUTOSCROLL_INTERVAL_TIME)
                    } else {
                        // autoscroll has come to an end (eye focus at the bottom)
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
                        playlistService.goToNextOrPrevious(+1)
                    } else {
                        val delay = remainingTimeMs.limitTo(1000)
                        onCountdownToNextSongRemainingTime(remainingTimeMs)
                        timerHandler.postDelayed(timerRunnable, delay)
                    }
                }

                else -> {}
            }
            null
        }
    }

    /**
     * @param dScroll line Part Scrolled
     * @param scroll  current scroll
     */
    private fun onCanvasScrollEvent(dScroll: Float, scroll: Float) {
        when (state) {
            AutoscrollState.WAITING -> {
                if (dScroll > 0) {
                    skipInitialPause()
                } else if (dScroll < 0) {
                    eyeFocus += dScroll * WAITING_TIME_ADJUST_MODIFIER
                    eyeFocus = eyeFocus.cutOffMin(0f)
                    showAutoscrollWaitingTime(remainingWaitingTimeS())
                }
            }
            AutoscrollState.SCROLLING -> {
                if (dScroll > 0) {
                    // speed up scrolling
                    val delta =
                        (dScroll * ADJUSTMENT_SPEED_SCALE).limitTo(ADJUSTMENT_MAX_SPEED_CHANGE)
                    autoAdjustScrollSpeed(delta)

                } else if (dScroll < 0) {

                    if (scroll <= 0) { // scrolling up to the beginning
                        // set counting down state with additional time
                        state = AutoscrollState.WAITING
                        eyeFocus += dScroll * WAITING_TIME_ADJUST_MODIFIER
                        previousStepTime = System.currentTimeMillis()
                        showAutoscrollWaitingTime(remainingWaitingTimeS())
                        return
                    } else {
                        // slow down scrolling
                        val delta =
                            -(-dScroll * ADJUSTMENT_SPEED_SCALE).limitTo(ADJUSTMENT_MAX_SPEED_CHANGE)
                        autoAdjustScrollSpeed(delta)
                    }
                }
            }
            AutoscrollState.ENDING -> {
                onAutoscrollStopUIEvent()
            }
            AutoscrollState.NEXT_SONG_COUNTDOWN -> {
                if (dScroll < 0) { // scroll up
                    onAutoscrollStopUIEvent()
                }
            }
            else -> {}
        }
    }

    private fun remainingWaitingTimeS(): Float {
        val halfScreenLines = songPreview?.let { songPreview ->
            songPreview.h.toFloat() / 2 / songPreview.lineheightPx
        } ?: 0f
        val remainingLines = halfScreenLines - eyeFocus
        return remainingLines / autoscrollSpeed
    }

    private fun showAutoscrollWaitingTime(s: Float) {
        val ms = when {
            s < 0 -> 0L
            else -> (s * 1000).toLong()
        }
        val seconds = ((ms + 500) / 1000).toString()
        uiInfoService.showInfoAction(
            R.string.autoscroll_starts_in,
            seconds,
            actionResId = R.string.action_start_now_autoscroll
        ) {
            this.skipInitialPause()
        }
    }

    private fun autoAdjustScrollSpeed(delta: Float) {
        if (autoSpeedAdjustment) {
            addAutoscrollSpeed(delta)
            logger.info("autoscroll speed adjusted: $autoscrollSpeed line / s")
        }
    }

    private fun skipInitialPause() {
        state = AutoscrollState.SCROLLING
        songPreview?.let { songPreview ->
            val eyeFocusPx = songPreview.scroll + songPreview.h / 2
            eyeFocus = eyeFocusPx / songPreview.lineheightPx
        }
        uiInfoService.clearSnackBars()
        onAutoscrollStartedEvent()
    }

    private fun onAutoscrollStartUIEvent() {
        if (!isRunning) {
            songPreview?.let { songPreview ->
                when {
                    songPreview.canScrollDown() -> {
                        start()
                        uiInfoService.showInfoAction(
                            R.string.autoscroll_started,
                            actionResId = R.string.action_stop_autoscroll,
                        ) {
                            this.stop()
                            songPreview.repaint()
                        }
                    }
                    canCountdownToNextSong() -> {
                        countdownToNextSong()
                    }
                    else -> {
                        uiInfoService.showInfo(R.string.end_of_song_autoscroll_stopped)
                    }
                }
            }
        }
    }

    private fun onAutoscrollStartedEvent() {
        uiInfoService.showInfoAction(
            R.string.autoscroll_started,
            actionResId = R.string.action_stop_autoscroll,
        ) {
            this.stop()
            songPreview?.repaint()
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
        return preferencesState.autoscrollForwardNextSong && playlistService.hasNextSong()
    }

    private fun countdownToNextSong() {
        state = AutoscrollState.NEXT_SONG_COUNTDOWN
        val visibleLinesAtEnd = songPreview?.visualLinesAtEnd ?: 0f
        val visibleLinesMillis = visibleLinesAtEnd / autoscrollSpeed * 1000
        var waitTimeMs: Long = when {
            (songPreview?.scroll ?: 0f) <= 0f -> { // at the beginning
                visibleLinesMillis
            }
            else -> {
                visibleLinesMillis * 0.5
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

    private fun addAutoscrollSpeed(delta: Float) {
        autoscrollSpeed = (autoscrollSpeed + delta).limitBetween(MIN_SPEED, MAX_SPEED)
        scrollSpeedAdjustmentSubject.onNext(autoscrollSpeed)
    }

    private fun persistIndividualSongSpeed() {
        if (preferencesState.autoscrollIndividualSpeed) {
            currentSongIdentifier?.let { currentSongIdentifier ->
                songsRepository.songTweakDao.setSongAutoscrollSpeed(
                    currentSongIdentifier,
                    autoscrollSpeed
                )
            }
        }
    }

}
