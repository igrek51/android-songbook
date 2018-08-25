package igrek.songbook.service.activity;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.window.WindowManagerService;

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
		try {
			windowManagerService.hideTaskbar();
			// force locale settings
			//setLocale("pl");
			layoutController.showFileList();
			logger.debug("Application has been initialized");
		} catch (Exception ex) {
			logger.fatal(activity, ex);
		}
	}
	
	private void setLocale(String langCode) {
		Resources res = activity.getResources();
		// Change locale settings in the app.
		DisplayMetrics dm = res.getDisplayMetrics();
		android.content.res.Configuration conf = res.getConfiguration();
		conf.locale = new Locale(langCode.toLowerCase());
		res.updateConfiguration(conf, dm);
	}
	
}
