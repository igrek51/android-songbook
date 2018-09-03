package igrek.songbook.service.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.preferences.PreferencesService;
import igrek.songbook.service.window.WindowManagerService;

public class ActivityController {
	
	@Inject
	WindowManagerService windowManagerService;
	@Inject
	Activity activity;
	@Inject
	PreferencesService preferencesService;
	
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
		logger.info("Activity has been destroyed.");
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
