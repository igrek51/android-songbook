package igrek.songbook.layout.settings;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.activity.ActivityController;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.LayoutController;
import igrek.songbook.layout.LayoutState;
import igrek.songbook.layout.MainLayout;
import igrek.songbook.layout.navigation.NavigationMenuController;
import igrek.songbook.layout.songpreview.LyricsManager;
import igrek.songbook.layout.songpreview.autoscroll.AutoscrollService;
import igrek.songbook.layout.view.ButtonClickEffect;
import igrek.songbook.layout.view.SliderController;
import igrek.songbook.persistence.preferences.PreferencesService;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SettingsLayoutController implements MainLayout {
	
	@Inject
	Lazy<ActivityController> activityController;
	@Inject
	LayoutController layoutController;
	@Inject
	UiInfoService uiInfoService;
	@Inject
	UiResourceService uiResourceService;
	@Inject
	AppCompatActivity activity;
	@Inject
	NavigationMenuController navigationMenuController;
	@Inject
	LyricsManager lyricsManager;
	@Inject
	AutoscrollService autoscrollService;
	@Inject
	PreferencesService preferencesService;
	
	private SliderController fontsizeSlider;
	private SliderController autoscrollPauseSlider;
	private SliderController autoscrollSpeedSlider;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public SettingsLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	@Override
	public void showLayout(View layout) {
		// Toolbar
		Toolbar toolbar1 = layout.findViewById(R.id.toolbar1);
		activity.setSupportActionBar(toolbar1);
		ActionBar actionBar = activity.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setDisplayShowHomeEnabled(false);
		}
		// navigation menu button
		ImageButton navMenuButton = layout.findViewById(R.id.navMenuButton);
		navMenuButton.setOnClickListener((v) -> navigationMenuController.navDrawerShow());
		ButtonClickEffect.addClickEffect(navMenuButton);
		
		SeekBar fontsizeSeekbar = layout.findViewById(R.id.fontsizeSeekbar);
		TextView fontsizeLabel = layout.findViewById(R.id.fontsizeLabel);
		SeekBar autoscrollPauseSeekbar = layout.findViewById(R.id.autoscrollPauseSeekbar);
		TextView autoscrollPauseLabel = layout.findViewById(R.id.autoscrollPauseLabel);
		SeekBar autoscrollSpeedSeekbar = layout.findViewById(R.id.autoscrollSpeedSeekbar);
		TextView autoscrollSpeedLabel = layout.findViewById(R.id.autoscrollSpeedLabel);
		
		float fontsize = lyricsManager.getFontsize();
		fontsizeSlider = new SliderController(fontsizeSeekbar, fontsizeLabel, fontsize, 5, 100) {
			@Override
			public String generateLabelText(float value) {
				return uiResourceService.resString(R.string.settings_font_size, roundDecimal(value, "#.#"));
			}
		};
		
		float autoscrollInitialPause = autoscrollService.getInitialPause();
		autoscrollPauseSlider = new SliderController(autoscrollPauseSeekbar, autoscrollPauseLabel, autoscrollInitialPause, 0, 90000) {
			@Override
			public String generateLabelText(float value) {
				return uiResourceService.resString(R.string.settings_scroll_initial_pause, msToS(value));
			}
		};
		
		float autoscrollSpeed = autoscrollService.getAutoscrollSpeed();
		autoscrollSpeedSlider = new SliderController(autoscrollSpeedSeekbar, autoscrollSpeedLabel, autoscrollSpeed, 0, 1.0f) {
			@Override
			public String generateLabelText(float value) {
				return uiResourceService.resString(R.string.settings_autoscroll_speed, roundDecimal(value, "#.####"));
			}
		};
		
		Observable.merge(fontsizeSlider.getValueSubject(), autoscrollPauseSlider.getValueSubject(), autoscrollSpeedSlider
				.getValueSubject())
				.debounce(200, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(value -> saveSettings());
		
		// TODO
		//		CheckBox fullscreenCheckbox = layout.findViewById(R.id.fullscreenCheckbox);
		//		Spinner chordsNotationSpinner = layout.findViewById(R.id.chordsNotationSpinner);
		
	}
	
	private String msToS(float ms) {
		return Integer.toString((int) ((ms + 500) / 1000));
	}
	
	private String roundDecimal(float f, String format) {
		DecimalFormat df = new DecimalFormat(format);
		df.setRoundingMode(RoundingMode.HALF_UP);
		return df.format(f);
	}
	
	private void saveSettings() {
		float fontsize = fontsizeSlider.getValue();
		lyricsManager.setFontsize(fontsize);
		float autoscrollInitialPause = autoscrollPauseSlider.getValue();
		autoscrollService.setInitialPause((int) autoscrollInitialPause);
		float autoscrollSpeed = autoscrollSpeedSlider.getValue();
		autoscrollService.setAutoscrollSpeed(autoscrollSpeed);
		
		preferencesService.saveAll();
	}
	
	@Override
	public LayoutState getLayoutState() {
		return LayoutState.SETTINGS;
	}
	
	@Override
	public int getLayoutResourceId() {
		return R.layout.settings;
	}
	
	@Override
	public void onBackClicked() {
		layoutController.showPreviousLayout();
	}
	
}
