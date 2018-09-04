package igrek.songbook.service.layout.help;

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

public class HelpLayoutController {
	
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
	
	public HelpLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showUIHelp() {
		String message = uiResourceService.resString(R.string.ui_help_content);
		String title = uiResourceService.resString(R.string.ui_help);
		uiInfoService.showDialog(title, message);
	}
}
