package igrek.songbook.service.activity;

import android.app.Activity;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.screen.WindowManagerService;

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
			
			// force locale
			//setLocale("pl");
			
			layoutController.showFileList();
			
			logger.debug("Application has been initialized");
			
		} catch (Exception ex) {
			logger.fatal(activity, ex);
		}
	}
	
}
