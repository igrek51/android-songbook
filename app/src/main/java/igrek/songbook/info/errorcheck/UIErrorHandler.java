package igrek.songbook.info.errorcheck;


import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;

public class UIErrorHandler {
	
	@Inject
	UiInfoService uiInfoService;
	@Inject
	UiResourceService uiResourceService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	private UIErrorHandler() {
	}
	
	public static void showError(Throwable t) {
		new UIErrorHandler()._handleError(t);
	}
	
	private void _handleError(Throwable t) {
		DaggerIoc.getFactoryComponent().inject(this);
		logger.error(t);
		uiInfoService.showInfo(uiResourceService.resString(R.string.error_occurred, t.getMessage()));
	}
	
}
