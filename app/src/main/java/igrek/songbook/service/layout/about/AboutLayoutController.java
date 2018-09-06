package igrek.songbook.service.layout.about;

import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.activity.ActivityController;
import igrek.songbook.service.info.UiInfoService;
import igrek.songbook.service.info.UiResourceService;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.secret.SecretUnlockerService;
import igrek.songbook.service.system.PackageInfoService;

public class AboutLayoutController {
	
	@Inject
	Lazy<ActivityController> activityController;
	@Inject
	LayoutController layoutController;
	@Inject
	UiInfoService uiInfoService;
	@Inject
	UiResourceService uiResourceService;
	@Inject
	AppCompatActivity activity;
	@Inject
	SecretUnlockerService secretUnlockerService;
	@Inject
	PackageInfoService packageInfoService;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public AboutLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showAbout() {
		String versionName = packageInfoService.getVersionName();
		String message = String.format(uiResourceService.resString(R.string.ui_about_content), versionName);
		String title = uiResourceService.resString(R.string.ui_about);
		String unlockActionName = uiResourceService.resString(R.string.unlock_action);
		Runnable unlockAction = secretUnlockerService::showUnlockAlert;
		uiInfoService.showDialog(title, message, unlockActionName, unlockAction);
	}
	
}
