package igrek.songbook.service.layout.search;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.activity.ActivityController;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.navmenu.NavigationMenuController;
import igrek.songbook.service.songtree.SongTreeItem;
import igrek.songbook.view.songselection.SongListView;

public class SongSearchLayoutController {
	
	@Inject
	Lazy<ActivityController> activityController;
	@Inject
	LayoutController layoutController;
	@Inject
	AppCompatActivity activity;
	@Inject
	NavigationMenuController navigationMenuController;
	
	private Logger logger = LoggerFactory.getLogger();
	private ActionBar actionBar;
	private SongListView itemsListView;
	private Integer scrollPos;
	
	public SongSearchLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showSongSearch(View layout) {
		// toolbar
		Toolbar toolbar1 = layout.findViewById(R.id.toolbar1);
		activity.setSupportActionBar(toolbar1);
		actionBar = activity.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setDisplayShowHomeEnabled(false);
		}
		
		ImageButton navMenuButton = layout.findViewById(R.id.navMenuButton);
		navMenuButton.setOnClickListener(v -> navigationMenuController.navDrawerShow());
		
		itemsListView = layout.findViewById(R.id.filesList);
		itemsListView.init(activity);
		
		itemsListView.setItems(new ArrayList<>());
	}
	
	public void scrollToItem(int position) {
		itemsListView.scrollTo(position);
	}
	
	public Integer getCurrentScrollPos() {
		return itemsListView.getCurrentScrollPosition();
	}
	
	public void scrollToPosition(int y) {
		itemsListView.scrollToPosition(y);
	}
	
	public void restoreScrollPosition() {
		if (scrollPos != null) {
			scrollToPosition(scrollPos);
		}
	}
	
	public void onItemClickedEvent(int posistion, SongTreeItem item) {
		scrollPos = getCurrentScrollPos();
		logger.debug("item clicked: " + item.getSimpleName());
	}
}
