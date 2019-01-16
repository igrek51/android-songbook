package igrek.songbook.songpreview.quickmenu;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.layout.view.SliderController;
import igrek.songbook.songpreview.autoscroll.AutoscrollService;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

@SuppressLint("CheckResult")
public class QuickMenuAutoscroll {
	
	@Inject
	UiResourceService uiResourceService;
	@Inject
	AutoscrollService autoscrollService;
	
	private boolean visible = false;
	private View quickMenuView;
	private Button autoscrollToggleButton;
	private SliderController autoscrollPauseSlider;
	private SliderController autoscrollSpeedSlider;
	
	public QuickMenuAutoscroll() {
		DaggerIoc.getFactoryComponent().inject(this);
		autoscrollService.getScrollStateSubject()
				.debounce(200, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(autoscrollState -> updateView());
		
		autoscrollService.getScrollSpeedAdjustmentSubject()
				.debounce(200, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(autoscrollSpeed -> updateView());
	}
	
	// TODO refactor: repeated code: same sliders in settings
	
	public void setQuickMenuView(View quickMenuView) {
		this.quickMenuView = quickMenuView;
		
		autoscrollToggleButton = quickMenuView.findViewById(R.id.autoscrollToggleButton);
		autoscrollToggleButton.setOnClickListener(v -> {
			if (!autoscrollService.isRunning())
				setVisible(false);
			
			autoscrollService.onAutoscrollToggleUIEvent();
		});
		
		// initial pause
		TextView initialPauseLabel = quickMenuView.findViewById(R.id.initialPauseLabel);
		SeekBar initialPauseSeekbar = quickMenuView.findViewById(R.id.initialPauseSeekbar);
		float autoscrollInitialPause = autoscrollService.getInitialPause();
		autoscrollPauseSlider = new SliderController(initialPauseSeekbar, initialPauseLabel, autoscrollInitialPause, 0, 90000) {
			@Override
			public String generateLabelText(float value) {
				return uiResourceService.resString(R.string.settings_scroll_initial_pause, roundDecimal(value / 1000, "##0"));
			}
		};
		ImageButton initialPauseMinusButton = quickMenuView.findViewById(R.id.initialPauseMinusButton);
		initialPauseMinusButton.setOnClickListener(v -> addInitialPause(-1000));
		ImageButton initialPausePlusButton = quickMenuView.findViewById(R.id.initialPausePlusButton);
		initialPausePlusButton.setOnClickListener(v -> addInitialPause(1000));
		
		// autoscroll speed
		TextView speedLabel = quickMenuView.findViewById(R.id.speedLabel);
		SeekBar speedSeekbar = quickMenuView.findViewById(R.id.speedSeekbar);
		float autoscrollSpeed = autoscrollService.getAutoscrollSpeed();
		autoscrollSpeedSlider = new SliderController(speedSeekbar, speedLabel, autoscrollSpeed, AutoscrollService.MIN_SPEED, AutoscrollService.MAX_SPEED) {
			@Override
			public String generateLabelText(float value) {
				return uiResourceService.resString(R.string.autoscroll_panel_speed, roundDecimal(value, "0.000"));
			}
		};
		ImageButton speedMinusButton = quickMenuView.findViewById(R.id.speedMinusButton);
		speedMinusButton.setOnClickListener(v -> addAutoscrollSpeed(-0.001f));
		ImageButton speedPlusButton = quickMenuView.findViewById(R.id.speedPlusButton);
		speedPlusButton.setOnClickListener(v -> addAutoscrollSpeed(0.001f));
		
		// save parameters on change
		Observable.merge(autoscrollPauseSlider.getValueSubject(), autoscrollSpeedSlider.getValueSubject())
				.debounce(200, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(value -> saveSettings());
	}
	
	private void addInitialPause(float diff) {
		float autoscrollInitialPause = autoscrollPauseSlider.getValue();
		autoscrollInitialPause += diff;
		autoscrollInitialPause = cutOff(autoscrollInitialPause, 0, 90000);
		autoscrollPauseSlider.setValue(autoscrollInitialPause);
		autoscrollService.setInitialPause((int) autoscrollInitialPause);
	}
	
	private void addAutoscrollSpeed(float diff) {
		float autoscrollSpeed = autoscrollSpeedSlider.getValue();
		autoscrollSpeed += diff;
		autoscrollSpeed = cutOff(autoscrollSpeed, 0, 1.0f);
		autoscrollSpeedSlider.setValue(autoscrollSpeed);
		autoscrollService.setAutoscrollSpeed(autoscrollSpeed);
	}
	
	private float cutOff(float value, float min, float max) {
		if (value < min)
			value = min;
		if (value > max)
			value = max;
		return value;
	}
	
	private void saveSettings() {
		float autoscrollInitialPause = autoscrollPauseSlider.getValue();
		autoscrollService.setInitialPause((int) autoscrollInitialPause);
		float autoscrollSpeed = autoscrollSpeedSlider.getValue();
		autoscrollService.setAutoscrollSpeed(autoscrollSpeed);
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		if (visible) {
			quickMenuView.setVisibility(View.VISIBLE);
			updateView();
		} else {
			quickMenuView.setVisibility(View.GONE);
		}
	}
	
	private void updateView() {
		if (visible) {
			// set toggle button text
			if (autoscrollToggleButton != null) {
				if (autoscrollService.isRunning()) {
					autoscrollToggleButton.setText(uiResourceService.resString(R.string.stop_autoscroll));
				} else {
					autoscrollToggleButton.setText(uiResourceService.resString(R.string.start_autoscroll));
				}
			}
			if (autoscrollPauseSlider != null && autoscrollSpeedSlider != null) {
				// set sliders value
				float autoscrollInitialPause = autoscrollService.getInitialPause();
				autoscrollPauseSlider.setValue(autoscrollInitialPause);
				float autoscrollSpeed = autoscrollService.getAutoscrollSpeed();
				autoscrollSpeedSlider.setValue(autoscrollSpeed);
			}
		}
	}
	
	private String roundDecimal(float f, String format) {
		DecimalFormat df = new DecimalFormat(format);
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(f);
	}
	
	/**
	 * @return is feature active - has impact on song preview (panel may be hidden)
	 */
	public boolean isFeatureActive() {
		return autoscrollService.isRunning();
	}
}
