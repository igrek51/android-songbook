package igrek.songbook.layout.about;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.about.secret.SecretUnlockerService;
import igrek.songbook.persistence.SongsDbRepository;
import igrek.songbook.system.PackageInfoService;

public class AboutLayoutController {
	
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
		String title = uiResourceService.resString(R.string.nav_about);
		String message = uiResourceService.resString(R.string.ui_about_content, appVersionName, dbVersionNumber);
		
		String unlockActionName = uiResourceService.resString(R.string.action_unlock);
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
