package igrek.songbook.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.model.chords.ChordsNotation;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.songpreview.LyricsManager;
import igrek.songbook.layout.songpreview.autoscroll.AutoscrollService;
import igrek.songbook.persistence.LocalDbService;
import igrek.songbook.persistence.preferences.PreferencesDefinition;
import igrek.songbook.persistence.preferences.PreferencesService;
import igrek.songbook.system.WindowManagerService;

public class ActivityController {
	
	@Inject
	WindowManagerService windowManagerService;
	@Inject
	Activity activity;
	@Inject
	PreferencesService preferencesService;
	@Inject
	LocalDbService localDbService;
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
		String orientationName = getOrientationName(newConfig.orientation);
		logger.debug("Screen resized: " + screenWidthDp + "dp x " + screenHeightDp + "dp - " + orientationName);
	}
	
	private String getOrientationName(int orientation) {
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			return "landscape";
		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			return "portrait";
		}
		return Integer.toString(orientation);
	}
	
	public void quit() {
		localDbService.closeDatabases();
		savePreferences();
		windowManagerService.keepScreenOn(false);
		activity.finish();
	}
	
	private void savePreferences() {
		preferencesService.setValue(PreferencesDefinition.fontsize, lyricsManager.getFontsize());
		preferencesService.setValue(PreferencesDefinition.autoscrollInitialPause, autoscrollService.getInitialPause());
		preferencesService.setValue(PreferencesDefinition.autoscrollSpeed, autoscrollService.getAutoscrollSpeed());
		
		ChordsNotation chordsNotation = lyricsManager.getChordsNotation();
		if (chordsNotation != null) {
			preferencesService.setValue(PreferencesDefinition.chordsNotationId, chordsNotation.getId());
		}
		
		preferencesService.saveAll();
	}
	
	public void onStart() {
	}
	
	public void onStop() {
		savePreferences();
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
