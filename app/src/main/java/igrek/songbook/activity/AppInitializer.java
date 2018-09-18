package igrek.songbook.activity;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

import javax.inject.Inject;

import igrek.songbook.BuildConfig;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.LayoutController;
import igrek.songbook.system.WindowManagerService;

public class AppInitializer {
	
	@Inject
	WindowManagerService windowManagerService;
	@Inject
	Activity activity;
	@Inject
	LayoutController layoutController;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public AppInitializer() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void init() {
		if (BuildConfig.DEBUG) {
			debugInit();
		}
		
		windowManagerService.hideTaskbar();
		layoutController.init();
		layoutController.showSongTree();
		
		logger.info("Application has been initialized.");
	}
	
	private void debugInit() {
		// Allow showing the activity even if the device is locked
		windowManagerService.showAppWhenLocked();
		setLocale("pl");
	}
	
	/**
	 * forces locale settings
	 * @param langCode language code (pl)
	 */
	private void setLocale(String langCode) {
		Resources res = activity.getResources();
		// Change locale settings in the app.
		DisplayMetrics dm = res.getDisplayMetrics();
		android.content.res.Configuration conf = res.getConfiguration();
		conf.locale = new Locale(langCode.toLowerCase());
		res.updateConfiguration(conf, dm);
	}
	
}
