package igrek.songbook.service.errorcheck;


import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.info.UIInfoService;

public class UIErrorHandler {
	
	@Inject
	UIInfoService UIInfoService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	private UIErrorHandler() {
	}
	
	public static void showError(Throwable t) {
		new UIErrorHandler()._handleError(t);
	}
	
	private void _handleError(Throwable t) {
		DaggerIoc.getFactoryComponent().inject(this);
		logger.error(t);
		UIInfoService.showInfo("Error occurred: " + t.getMessage());
	}
	
}
