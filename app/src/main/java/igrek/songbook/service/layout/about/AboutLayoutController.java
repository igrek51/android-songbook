package igrek.songbook.service.layout.about;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.widget.EditText;

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
	
	private Logger logger = LoggerFactory.getLogger();
	
	public AboutLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showAbout() {
		String message = uiResourceService.resString(R.string.ui_about_content);
		String title = uiResourceService.resString(R.string.ui_about);
		String unlockActionName = uiResourceService.resString(R.string.unlock_action);
		Runnable unlockAction = () -> showUnlock();
		uiInfoService.showDialog(title, message, unlockActionName, unlockAction);
	}
	
	public void showUnlock() {
		String unlockAction = uiResourceService.resString(R.string.unlock_action);
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
		dlgAlert.setMessage("Type in a secret key:");
		dlgAlert.setTitle(unlockAction);
		
		final EditText input = new EditText(activity);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		dlgAlert.setView(input);
		
		dlgAlert.setPositiveButton(unlockAction, (dialog, which) -> {
		});
		dlgAlert.setNegativeButton(uiResourceService.resString(R.string.action_cancel), (dialog, which) -> {
		});
		dlgAlert.setCancelable(true);
		dlgAlert.create().show();
	}
	
}
