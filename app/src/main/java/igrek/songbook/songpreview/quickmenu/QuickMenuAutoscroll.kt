package igrek.songbook.songpreview.quickmenu

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.slider.SliderController
import igrek.songbook.songpreview.autoscroll.AutoscrollService
import igrek.songbook.util.limitBetween
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class QuickMenuAutoscroll(
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
        autoscrollService: LazyInject<AutoscrollService> = appFactory.autoscrollService,
) {
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val autoscrollService by LazyExtractor(autoscrollService)

    var isVisible = false
        set(visible) {
            field = visible
            if (visible) {
                quickMenuView?.visibility = View.VISIBLE
                updateView()
            } else {
                quickMenuView?.visibility = View.GONE
            }
        }
    private var quickMenuView: View? = null
    private var autoscrollToggleButton: Button? = null
    private var autoscrollPauseSlider: SliderController? = null
    private var autoscrollSpeedSlider: SliderController? = null

    private val subscriptions = mutableListOf<Disposable>()

    /**
     * @return is feature active - has impact on song preview (panel may be hidden)
     */
    val isFeatureActive: Boolean
        get() = autoscrollService.isRunning

    init {
        this.autoscrollService.scrollStateSubject
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    updateView()
                }, UiErrorHandler::handleError)
        this.autoscrollService.scrollSpeedAdjustmentSubject
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    updateView()
                }, UiErrorHandler::handleError)
    }

    fun setQuickMenuView(quickMenuView: View) {
        this.quickMenuView = quickMenuView

        autoscrollToggleButton = quickMenuView.findViewById(R.id.autoscrollToggleButton)
        autoscrollToggleButton?.setOnClickListener {
            if (!autoscrollService.isRunning)
                isVisible = false

            autoscrollService.onAutoscrollToggleUIEvent()
        }

        // initial pause
        val initialPauseLabel = quickMenuView.findViewById<TextView>(R.id.initialPauseLabel)
        val initialPauseSeekbar = quickMenuView.findViewById<SeekBar>(R.id.initialPauseSeekbar)
        val autoscrollInitialPause = autoscrollService.initialPause.toFloat()
        autoscrollPauseSlider = object : SliderController(initialPauseSeekbar, initialPauseLabel, autoscrollInitialPause, 0f, 90000f) {
            override fun generateLabelText(value: Float): String {
                return uiResourceService.resString(R.string.settings_scroll_initial_pause, roundDecimal(value / 1000, "##0"))
            }
        }
        val initialPauseMinusButton = quickMenuView.findViewById<ImageButton>(R.id.initialPauseMinusButton)
        initialPauseMinusButton.setOnClickListener { addInitialPause(-1000f) }
        val initialPausePlusButton = quickMenuView.findViewById<ImageButton>(R.id.initialPausePlusButton)
        initialPausePlusButton.setOnClickListener { addInitialPause(1000f) }

        // autoscroll speed
        val speedLabel = quickMenuView.findViewById<TextView>(R.id.speedLabel)
        val speedSeekbar = quickMenuView.findViewById<SeekBar>(R.id.speedSeekbar)
        val autoscrollSpeed = autoscrollService.autoscrollSpeed
        autoscrollSpeedSlider = object : SliderController(speedSeekbar, speedLabel, autoscrollSpeed, AutoscrollService.MIN_SPEED, AutoscrollService.MAX_SPEED) {
            override fun generateLabelText(value: Float): String {
                return uiResourceService.resString(R.string.autoscroll_panel_speed, roundDecimal(value, "0.000"))
            }
        }
        val speedMinusButton = quickMenuView.findViewById<ImageButton>(R.id.speedMinusButton)
        speedMinusButton.setOnClickListener { addAutoscrollSpeed(-0.001f) }
        val speedPlusButton = quickMenuView.findViewById<ImageButton>(R.id.speedPlusButton)
        speedPlusButton.setOnClickListener { addAutoscrollSpeed(0.001f) }

        // save parameters on change
        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(
                Observable.merge(
                        autoscrollPauseSlider!!.valueSubject,
                        autoscrollSpeedSlider!!.valueSubject)
                        .debounce(200, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            saveSettings()
                        }, UiErrorHandler::handleError))
    }

    private fun addInitialPause(diff: Float) {
        autoscrollPauseSlider?.let { autoscrollPauseSlider ->
            var autoscrollInitialPause = autoscrollPauseSlider.value
            autoscrollInitialPause += diff
            autoscrollInitialPause = autoscrollInitialPause.limitBetween(0f, 90000f)
            autoscrollPauseSlider.value = autoscrollInitialPause
            autoscrollService.initialPause = autoscrollInitialPause.toLong()
        }
    }

    private fun addAutoscrollSpeed(diff: Float) {
        autoscrollSpeedSlider?.let { autoscrollSpeedSlider ->
            var autoscrollSpeed = autoscrollSpeedSlider.value
            autoscrollSpeed += diff
            autoscrollSpeed = autoscrollSpeed.limitBetween(0f, 1.0f)
            autoscrollSpeedSlider.value = autoscrollSpeed
        }
    }

    private fun saveSettings() {
        autoscrollPauseSlider?.let { autoscrollPauseSlider ->
            val autoscrollInitialPause = autoscrollPauseSlider.value
            autoscrollService.initialPause = autoscrollInitialPause.toLong()
        }
        autoscrollSpeedSlider?.let { autoscrollSpeedSlider ->
            val autoscrollSpeed = autoscrollSpeedSlider.value
            autoscrollService.autoscrollSpeed = autoscrollSpeed
        }
    }

    private fun updateView() {
        if (isVisible) {
            // set toggle button text
            if (autoscrollService.isRunning) {
                autoscrollToggleButton?.text = uiResourceService.resString(R.string.stop_autoscroll)
            } else {
                autoscrollToggleButton?.text = uiResourceService.resString(R.string.start_autoscroll)
            }
            // set sliders value
            val autoscrollInitialPause = autoscrollService.initialPause.toFloat()
            autoscrollPauseSlider?.value = autoscrollInitialPause
            val autoscrollSpeed = autoscrollService.autoscrollSpeed
            autoscrollSpeedSlider?.value = autoscrollSpeed
        }
    }

    private fun roundDecimal(f: Float, format: String): String {
        val df = DecimalFormat(format)
        df.roundingMode = RoundingMode.HALF_UP
        return df.format(f.toDouble())
    }
}
