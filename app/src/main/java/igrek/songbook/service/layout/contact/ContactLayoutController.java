package igrek.songbook.service.layout.contact;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.activity.ActivityController;
import igrek.songbook.service.errorcheck.SafeClickListener;
import igrek.songbook.service.info.UiInfoService;
import igrek.songbook.service.info.UiResourceService;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.navmenu.NavigationMenuController;

public class ContactLayoutController {
	
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
	NavigationMenuController navigationMenuController;
	
	private Logger logger = LoggerFactory.getLogger();
	private EditText contactMessageEdit;
	
	public ContactLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showLayout(View layout) {
		contactMessageEdit = layout.findViewById(R.id.contactMessageEdit);
		Button contactSendButton = layout.findViewById(R.id.contactSendButton);
		contactSendButton.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				sendContactMessage();
			}
		});
		
	}
	
	private void sendContactMessage() {
		String message = contactMessageEdit.getText().toString();
		// TODO
		uiInfoService.showToast("not implemented yet");
	}
}
