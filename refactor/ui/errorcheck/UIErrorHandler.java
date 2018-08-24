package igrek.todotree.ui.errorcheck;


import javax.inject.Inject;

import igrek.songbook.service.info.UserInfoService;
import igrek.todotree.dagger.DaggerIOC;
import igrek.todotree.exceptions.DatabaseLockedException;
import igrek.todotree.logger.Logger;

public class UIErrorHandler {
	
	@Inject
	UserInfoService userInfoService2;
	
	@Inject
	Logger logger;
	
	private void _handleError(Throwable t) {
		DaggerIOC.getFactoryComponent().inject(this);
		// database locked
		if (t instanceof DatabaseLockedException) {
			logger.warn(t.getMessage());
			userInfoService2.showInfo(t.getMessage());
			return;
		}
		logger.error(t);
		userInfoService2.showInfo("Error occurred: " + t.getMessage());
	}
	
	public static void showError(Throwable t) {
		new UIErrorHandler()._handleError(t);
	}
	
}
