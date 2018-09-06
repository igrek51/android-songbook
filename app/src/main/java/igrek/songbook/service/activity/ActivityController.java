package igrek.songbook.service.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.chords.LyricsManager;
import igrek.songbook.service.persistence.database.LocalDatabaseService;
import igrek.songbook.service.preferences.PreferencesDefinition;
import igrek.songbook.service.preferences.PreferencesService;
import igrek.songbook.service.system.WindowManagerService;

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
	
	public void onDestroy() {
		localDatabaseService.closeDatabase();
		savePreferences();
		logger.info("Activity has been destroyed.");
	}
	
	private void savePreferences() {
		float fontsize = lyricsManager.getFontsize();
		preferencesService.setValue(PreferencesDefinition.fontsize, fontsize);
		preferencesService.saveAll();
	}
	
	public void onStart() {
	}
	
	public void onStop() {
	}
	
	public void quit() {
		preferencesService.saveAll();
		windowManagerService.keepScreenOn(false);
		activity.finish();
	}
	
	public void minimize() {
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(startMain);
	}
	
}
