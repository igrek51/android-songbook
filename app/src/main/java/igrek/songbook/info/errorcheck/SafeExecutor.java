package igrek.songbook.info.errorcheck;


import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;

public class SafeExecutor {
	
	@Inject
	UiInfoService uiInfoService;
	@Inject
	UiResourceService uiResourceService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public void execute(Runnable action) {
		try {
			action.run();
		} catch (Throwable t) {
			logger.error(t);
			DaggerIoc.getFactoryComponent().inject(this);
			uiInfoService.showInfo(uiResourceService.resString(R.string.error_occurred, t.getMessage()));
		}
	}
	
}
