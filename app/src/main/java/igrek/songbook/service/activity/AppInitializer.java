package igrek.songbook.service.activity;

import android.app.Activity;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.screen.ScreenService;

public class AppInitializer {
	
	@Inject
	ScreenService screenService;
	@Inject
	Activity activity;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public AppInitializer() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void init() {
		try {
			screenService.hideTaskbar();
			
			// force locale
			//setLocale("pl");
			
			// TODO
			//			gui = new GUI(activity);
			//			gui.showFileList(fileTreeManager.getCurrentDirName(), fileTreeManager.getItems());
			
			logger.debug("Application has been initialized");
			
		} catch (Exception ex) {
			logger.fatal(activity, ex);
		}
	}
	
}
