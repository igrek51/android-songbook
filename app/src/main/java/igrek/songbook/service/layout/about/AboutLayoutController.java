package igrek.songbook.service.layout.about;

import android.support.v7.app.AlertDialog;
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
import igrek.songbook.service.persistence.SongsDbRepository;
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
	@Inject
	SongsDbRepository songsDbRepository;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public AboutLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showAbout() {
		String appVersionName = packageInfoService.getVersionName();
		String dbVersionNumber = Long.toString(songsDbRepository.getSongsDb().getVersionNumber());
		String title = uiResourceService.resString(R.string.ui_about);
		String message = uiResourceService.resString(R.string.ui_about_content, appVersionName, dbVersionNumber);
		
		String unlockActionName = uiResourceService.resString(R.string.unlock_action);
		Runnable unlockAction = secretUnlockerService::showUnlockAlert;
		showDialogWithActions(title, message, unlockActionName, unlockAction);
	}
	
	private void showDialogWithActions(String title, String message, String neutralActionName, Runnable neutralAction) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
		alertBuilder.setMessage(message);
		alertBuilder.setTitle(title);
		alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_info_ok), (dialog, which) -> {
		});
		alertBuilder.setNeutralButton(neutralActionName, (dialog, which) -> neutralAction.run());
		alertBuilder.setCancelable(true);
		AlertDialog alertDialog = alertBuilder.create();
		// set button almost hidden by setting color
		alertDialog.setOnShowListener(arg0 -> alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
				.setTextColor(uiResourceService.getColor(R.color.unlockAction)));
		
		alertDialog.show();
	}
	
}
