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
import igrek.songbook.songpreview.scroll.AutoscrollService
import igrek.songbook.util.limitBetween
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

// Singleton
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

        // autoscroll speed
        val speedLabel = quickMenuView.findViewById<TextView>(R.id.speedLabel)
        val speedSeekbar = quickMenuView.findViewById<SeekBar>(R.id.speedSeekbar)
        val autoscrollSpeed = autoscrollService.autoscrollSpeed
        autoscrollSpeedSlider = object : SliderController(
            speedSeekbar,
            speedLabel,
            autoscrollSpeed,
            AutoscrollService.MIN_SPEED,
            AutoscrollService.MAX_SPEED
        ) {
            override fun generateLabelText(value: Float): String {
                return uiResourceService.resString(
                    R.string.autoscroll_panel_speed,
                    roundDecimal(value, "0.000")
                )
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
            autoscrollSpeedSlider!!.valueSubject
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    saveSettings()
                }, UiErrorHandler::handleError)
        )
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
                autoscrollToggleButton?.text =
                    uiResourceService.resString(R.string.start_autoscroll)
            }
            // set sliders value
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
