package igrek.songbook.layout.songpreview.view.quickmenu;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.layout.songpreview.LyricsManager;
import igrek.songbook.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.layout.songpreview.autoscroll.AutoscrollService;
import igrek.songbook.layout.songpreview.view.SongPreview;
import igrek.songbook.layout.view.SliderController;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class QuickMenuAutoscroll {
	
	@Inject
	Lazy<LyricsManager> lyricsManager;
	@Inject
	UiResourceService uiResourceService;
	@Inject
	AutoscrollService autoscrollService;
	@Inject
	Lazy<SongPreviewLayoutController> songPreviewController;
	
	private boolean visible = false;
	private View quickMenuView;
	private TextView initialPauseLabel;
	private SeekBar initialPauseSeekbar;
	private TextView speedLabel;
	private SeekBar speedSeekbar;
	private Button autoscrollToggleButton;
	private SliderController autoscrollPauseSlider;
	private SliderController autoscrollSpeedSlider;
	
	public QuickMenuAutoscroll() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	private SongPreview getCanvas() {
		return songPreviewController.get().getSongPreview();
	}
	
	public void setQuickMenuView(View quickMenuView) {
		this.quickMenuView = quickMenuView;
		
		autoscrollToggleButton = quickMenuView.findViewById(R.id.autoscrollToggleButton);
		autoscrollToggleButton.setOnClickListener(v -> {
			if (autoscrollService.isRunning()) {
				autoscrollService.onAutoscrollStopUIEvent();
			} else {
				autoscrollService.onAutoscrollStartUIEvent();
			}
			updateView();
		});
		
		initialPauseLabel = quickMenuView.findViewById(R.id.initialPauseLabel);
		ImageButton initialPauseMinusButton = quickMenuView.findViewById(R.id.initialPauseMinusButton);
		ImageButton initialPausePlusButton = quickMenuView.findViewById(R.id.initialPausePlusButton);
		initialPauseSeekbar = quickMenuView.findViewById(R.id.initialPauseSeekbar);
		
		speedLabel = quickMenuView.findViewById(R.id.speedLabel);
		ImageButton speedMinusButton = quickMenuView.findViewById(R.id.speedMinusButton);
		ImageButton speedPlusButton = quickMenuView.findViewById(R.id.speedPlusButton);
		speedSeekbar = quickMenuView.findViewById(R.id.speedSeekbar);
		
		float autoscrollInitialPause = autoscrollService.getInitialPause();
		autoscrollPauseSlider = new SliderController(initialPauseSeekbar, initialPauseLabel, autoscrollInitialPause, 0, 90000) {
			@Override
			public String generateLabelText(float value) {
				return uiResourceService.resString(R.string.settings_scroll_initial_pause, Integer.toString((int) (value / 1000)));
			}
		};
		
		float autoscrollSpeed = autoscrollService.getAutoscrollSpeed();
		autoscrollSpeedSlider = new SliderController(speedSeekbar, speedLabel, autoscrollSpeed, 0, 1.0f) {
			@Override
			public String generateLabelText(float value) {
				return uiResourceService.resString(R.string.settings_autoscroll_speed, roundDecimal(value, "#.####"));
			}
		};
		
		Observable.merge(autoscrollPauseSlider.getValueSubject(), autoscrollSpeedSlider.getValueSubject())
				.debounce(200, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(value -> saveSettings());
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
		// set toggle button text
		if (autoscrollService.isRunning()) {
			autoscrollToggleButton.setText(uiResourceService.resString(R.string.stop_autoscroll));
		} else {
			autoscrollToggleButton.setText(uiResourceService.resString(R.string.start_autoscroll));
		}
		// set sliders value
		float autoscrollInitialPause = autoscrollService.getInitialPause();
		autoscrollPauseSlider.setValue(autoscrollInitialPause);
		float autoscrollSpeed = autoscrollService.getAutoscrollSpeed();
		autoscrollSpeedSlider.setValue(autoscrollSpeed);
	}
	
	private String roundDecimal(float f, String format) {
		DecimalFormat df = new DecimalFormat(format);
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(f);
	}
	
}
