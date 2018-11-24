package igrek.songbook.layout;

import android.app.Activity;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.contact.ContactLayoutController;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.navigation.NavigationMenuController;
import igrek.songbook.settings.SettingsLayoutController;
import igrek.songbook.songedit.EditSongLayoutController;
import igrek.songbook.songpreview.SongPreviewLayoutController;
import igrek.songbook.songselection.favourite.FavouritesLayoutController;
import igrek.songbook.songselection.songsearch.SongSearchLayoutController;
import igrek.songbook.songselection.songtree.SongTreeLayoutController;

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
	Lazy<EditSongLayoutController> importSongLayoutController;
	@Inject
	Lazy<FavouritesLayoutController> favouritesLayoutController;
	
	@Inject
	Activity activity;
	
	private CoordinatorLayout mainContentLayout;
	private Logger logger = LoggerFactory.getLogger();
	private MainLayout previouslyShownLayout;
	private MainLayout currentlyShownLayout;
	private MainLayout lastSongSelectionLayout;
	
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
		lastSongSelectionLayout = songTreeController.get();
	}
	
	public void showSongSearch() {
		showMainLayout(songSearchController.get());
		lastSongSelectionLayout = songSearchController.get();
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
	
	public void showImportSong() {
		showMainLayout(importSongLayoutController.get());
	}
	
	public void showFavourites() {
		showMainLayout(favouritesLayoutController.get());
		lastSongSelectionLayout = favouritesLayoutController.get();
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
	
	public void showLastSongSelectionLayout() {
		if (lastSongSelectionLayout != null) {
			showMainLayout(lastSongSelectionLayout);
		}
	}
	
	public boolean isState(LayoutState compare) {
		return state == compare;
	}
	
	public void onBackClicked() {
		currentlyShownLayout.onBackClicked();
	}
}
