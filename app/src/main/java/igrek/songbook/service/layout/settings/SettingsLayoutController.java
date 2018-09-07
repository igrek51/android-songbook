package igrek.songbook.service.layout.settings;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.activity.ActivityController;
import igrek.songbook.service.chords.LyricsManager;
import igrek.songbook.service.info.UiInfoService;
import igrek.songbook.service.info.UiResourceService;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.layout.LayoutState;
import igrek.songbook.service.layout.MainLayout;
import igrek.songbook.service.navmenu.NavigationMenuController;

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
		
		TextView fontsizeLabel = layout.findViewById(R.id.fontsizeLabel);
		SeekBar fontsizeSeekbar = layout.findViewById(R.id.fontsizeSeekbar);
		TextView autoscrollPauseLabel = layout.findViewById(R.id.autoscrollPauseLabel);
		SeekBar autoscrollPauseSeekbar = layout.findViewById(R.id.autoscrollPauseSeekbar);
		TextView autoscrollSpeedLabel = layout.findViewById(R.id.autoscrollSpeedLabel);
		SeekBar autoscrollSpeedSeekbar = layout.findViewById(R.id.autoscrollSpeedSeekbar);
		CheckBox fullscreenCheckbox = layout.findViewById(R.id.fullscreenCheckbox);
		Spinner chordsNotationSpinner = layout.findViewById(R.id.chordsNotationSpinner);
		
		fontsizeSeekbar.setMax(100);
		fontsizeSeekbar.setProgress(80);
		fontsizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				float minValue = 5;
				float maxValue = 50;
				
				float value = minValue + (maxValue - minValue) * progress / seekBar.getMax();
				
				String label = uiResourceService.resString(R.string.settings_font_size, Float.toString(value));
				fontsizeLabel.setText(label);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		int progress = fontsizeSeekbar.getProgress();
		
	}
	
	@Override
	public LayoutState getLayoutState() {
		return LayoutState.SETTINGS;
	}
	
	@Override
	public int getLayoutResourceId() {
		return R.layout.settings;
	}
	
}
