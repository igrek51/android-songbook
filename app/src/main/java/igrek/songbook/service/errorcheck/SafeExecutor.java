package igrek.songbook.service.errorcheck;


import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.info.UIInfoService;

public class SafeExecutor {
	
	@Inject
	UIInfoService UIInfoService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public void execute(Runnable action) {
		try {
			action.run();
		} catch (Throwable t) {
			logger.error(t);
			DaggerIoc.getFactoryComponent().inject(this);
			UIInfoService.showInfo("Error occurred: " + t.getMessage());
		}
	}
	
}
