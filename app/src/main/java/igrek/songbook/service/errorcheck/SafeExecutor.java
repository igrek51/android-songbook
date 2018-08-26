package igrek.songbook.service.errorcheck;


import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.info.UserInfoService;

public class SafeExecutor {
	
	@Inject
	UserInfoService userInfoService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public void execute(Runnable action) {
		try {
			action.run();
		} catch (Throwable t) {
			logger.error(t);
			DaggerIoc.getFactoryComponent().inject(this);
			userInfoService.showInfo("Error occurred: " + t.getMessage());
		}
	}
	
}
