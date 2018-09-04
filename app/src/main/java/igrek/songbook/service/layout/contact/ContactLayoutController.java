package igrek.songbook.service.layout.contact;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

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
import igrek.songbook.service.navmenu.NavigationMenuController;
import igrek.songbook.view.songselection.SongListView;

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
	private ActionBar actionBar;
	private SongListView itemsListView;
	private TextView toolbarTitle;
	
	public ContactLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showContact() {
		String message = uiResourceService.resString(R.string.ui_contact_content);
		String title = uiResourceService.resString(R.string.ui_contact);
		uiInfoService.showDialog(title, message);
	}
}
