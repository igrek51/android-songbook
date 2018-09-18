package igrek.songbook.layout.contact;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.activity.ActivityController;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.errorcheck.SafeClickListener;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.LayoutController;
import igrek.songbook.layout.LayoutState;
import igrek.songbook.layout.MainLayout;
import igrek.songbook.layout.navigation.NavigationMenuController;
import igrek.songbook.layout.view.ButtonClickEffect;

public class ContactLayoutController implements MainLayout {
	
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
	@Inject
	SendFeedbackService sendFeedbackService;
	
	private Logger logger = LoggerFactory.getLogger();
	private EditText contactMessageEdit;
	private EditText contactAuthorEdit;
	
	public ContactLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	@Override
	public void showLayout(View layout) {
		// Toolbar
		Toolbar toolbar1 = layout.findViewById(R.id.toolbar1);
		activity.setSupportActionBar(toolbar1);
		ActionBar actionBar = activity.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setDisplayShowHomeEnabled(false);
		}
		// navigation menu button
		ImageButton navMenuButton = layout.findViewById(R.id.navMenuButton);
		navMenuButton.setOnClickListener((v) -> navigationMenuController.navDrawerShow());
		ButtonClickEffect.addClickEffect(navMenuButton);
		
		contactMessageEdit = layout.findViewById(R.id.contactMessageEdit);
		contactAuthorEdit = layout.findViewById(R.id.contactAuthorEdit);
		Button contactSendButton = layout.findViewById(R.id.contactSendButton);
		contactSendButton.setOnClickListener(new SafeClickListener() {
			@Override
			public void onClick() {
				sendContactMessage();
			}
		});
	}
	
	@Override
	public LayoutState getLayoutState() {
		return LayoutState.CONTACT;
	}
	
	@Override
	public int getLayoutResourceId() {
		return R.layout.contact;
	}
	
	@Override
	public void onBackClicked() {
		layoutController.showSongTree();
	}
	
	private void sendContactMessage() {
		String message = contactMessageEdit.getText().toString();
		String author = contactAuthorEdit.getText().toString();
		if (message == null || message.isEmpty()) {
			uiInfoService.showToast("Message field can't be empty.");
			return;
		}
		sendFeedbackService.sendFeedback(message, author);
	}
}
