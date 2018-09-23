package igrek.songbook.layout;

import android.app.Activity;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.contact.ContactLayoutController;
import igrek.songbook.layout.navigation.NavigationMenuController;
import igrek.songbook.layout.settings.SettingsLayoutController;
import igrek.songbook.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.layout.songsearch.SongSearchLayoutController;
import igrek.songbook.layout.songtree.SongTreeLayoutController;

public class LayoutController {
	
	@Inject
	Lazy<SongTreeLayoutController> songTreeController;
	@Inject
	Lazy<SongSearchLayoutController> songSearchController;
	@Inject
	Lazy<SongPreviewLayoutController> songPreviewController;
	@Inject
	Lazy<ContactLayoutController> contactLayoutController;
	@Inject
	Lazy<NavigationMenuController> navigationMenuController;
	@Inject
	Lazy<SettingsLayoutController> settingsLayoutController;
	
	@Inject
	Activity activity;
	
	private CoordinatorLayout mainContentLayout;
	private Logger logger = LoggerFactory.getLogger();
	private MainLayout previouslyShownLayout;
	private MainLayout currentlyShownLayout;
	
	private LayoutState state = LayoutState.SONGS_TREE;
	
	public LayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void init() {
		activity.setContentView(R.layout.main_layout);
		mainContentLayout = activity.findViewById(R.id.main_content);
		navigationMenuController.get().init();
	}
	
	public void showSongTree() {
		showMainLayout(songTreeController.get());
	}
	
	public void showSongSearch() {
		showMainLayout(songSearchController.get());
	}
	
	public void showSongPreview() {
		showMainLayout(songPreviewController.get());
	}
	
	public void showContact() {
		showMainLayout(contactLayoutController.get());
	}
	
	public void showSettings() {
		showMainLayout(settingsLayoutController.get());
	}
	
	
	private void showMainLayout(MainLayout mainLayout) {
		// leave previous (current) layout
		if (currentlyShownLayout != null)
			currentlyShownLayout.onLayoutExit();
		
		previouslyShownLayout = currentlyShownLayout;
		currentlyShownLayout = mainLayout;
		
		int layoutResource = mainLayout.getLayoutResourceId();
		state = mainLayout.getLayoutState();
		
		// replace main content with brand new inflated layout
		mainContentLayout.removeAllViews();
		LayoutInflater inflater = activity.getLayoutInflater();
		View layoutView = inflater.inflate(layoutResource, null);
		layoutView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mainContentLayout.addView(layoutView);
		
		mainLayout.showLayout(layoutView);
	}
	
	public void showPreviousLayout() {
		if (previouslyShownLayout != null) {
			showMainLayout(previouslyShownLayout);
		}
	}
	
	public boolean isState(LayoutState compare) {
		return state == compare;
	}
	
	public void onBackClicked() {
		currentlyShownLayout.onBackClicked();
	}
}
