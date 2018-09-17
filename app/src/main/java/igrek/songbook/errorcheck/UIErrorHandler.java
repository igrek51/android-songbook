package igrek.songbook.errorcheck;


import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.info.UiInfoService;

public class UIErrorHandler {
	
	@Inject
	UiInfoService uiInfoService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	private UIErrorHandler() {
	}
	
	public static void showError(Throwable t) {
		new UIErrorHandler()._handleError(t);
	}
	
	private void _handleError(Throwable t) {
		DaggerIoc.getFactoryComponent().inject(this);
		logger.error(t);
		uiInfoService.showInfo("Error occurred: " + t.getMessage());
	}
	
}
