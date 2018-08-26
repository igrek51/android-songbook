package igrek.songbook.service.errorcheck;


import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.info.UserInfoService;

public class UIErrorHandler {
	
	@Inject
	UserInfoService userInfoService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	private UIErrorHandler() {
	}
	
	private void _handleError(Throwable t) {
		DaggerIoc.getFactoryComponent().inject(this);
		logger.error(t);
		userInfoService.showInfo("Error occurred: " + t.getMessage());
	}
	
	public static void showError(Throwable t) {
		new UIErrorHandler()._handleError(t);
	}
	
}
