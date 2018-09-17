package igrek.songbook.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.chords.autoscroll.AutoscrollService;
import igrek.songbook.chords.LyricsManager;
import igrek.songbook.persistence.LocalDatabaseService;
import igrek.songbook.settings.preferences.PreferencesDefinition;
import igrek.songbook.settings.preferences.PreferencesService;
import igrek.songbook.system.WindowManagerService;

public class ActivityController {
	
	@Inject
	WindowManagerService windowManagerService;
	@Inject
	Activity activity;
	@Inject
	PreferencesService preferencesService;
	@Inject
	LocalDatabaseService localDatabaseService;
	@Inject
	LyricsManager lyricsManager;
	@Inject
	AutoscrollService autoscrollService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public ActivityController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		// resize event
		int screenWidthDp = newConfig.screenWidthDp;
		int screenHeightDp = newConfig.screenHeightDp;
		int orientation = newConfig.orientation;
		int densityDpi = newConfig.densityDpi;
		logger.debug("Screen resized: " + screenWidthDp + "dp x " + screenHeightDp + "dp (DPI = " + densityDpi + ")");
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			logger.debug("Screen orientation: landscape");
		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			logger.debug("Screen orientation: portrait");
		}
	}
	
	public void quit() {
		localDatabaseService.closeDatabase();
		savePreferences();
		windowManagerService.keepScreenOn(false);
		activity.finish();
	}
	
	private void savePreferences() {
		preferencesService.setValue(PreferencesDefinition.fontsize, lyricsManager.getFontsize());
		preferencesService.setValue(PreferencesDefinition.autoscrollInitialPause, autoscrollService.getInitialPause());
		preferencesService.setValue(PreferencesDefinition.autoscrollSpeed, autoscrollService.getAutoscrollSpeed());
		preferencesService.saveAll();
	}
	
	public void onStart() {
	}
	
	public void onStop() {
	}
	
	public void onDestroy() {
		logger.info("Activity has been destroyed.");
	}
	
	public void minimize() {
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(startMain);
	}
	
}
