package igrek.songbook.service.layout;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.layout.navmenu.NavigationMenuController;
import igrek.songbook.service.layout.songpreview.SongPreviewController;
import igrek.songbook.service.layout.songselection.SongSelectionController;

public class LayoutController {
	
	@Inject
	Lazy<SongSelectionController> songSelectionController;
	
	@Inject
	Lazy<SongPreviewController> songPreviewController;
	
	@Inject
	Lazy<NavigationMenuController> navigationMenuController;
	
	@Inject
	Activity activity;
	
	private FrameLayout mainContentLayout;
	private Logger logger = LoggerFactory.getLogger();
	
	private LayoutState state = LayoutState.SONGS_LIST;
	
	public LayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void init() {
		activity.setContentView(R.layout.main_layout);
		mainContentLayout = activity.findViewById(R.id.main_content);
		navigationMenuController.get().init();
	}
	
	public void showSongSelection() {
		View layout = setMainContentLayout(R.layout.files_list);
		songSelectionController.get().showSongSelection(layout);
	}
	
	public void showSongPreview() {
		View layout = setMainContentLayout(R.layout.file_content);
		songPreviewController.get().showSongPreview(layout);
	}
	
	public boolean isState(LayoutState compare) {
		return state == compare;
	}
	
	public void setState(LayoutState state) {
		this.state = state;
	}
	
	private View setMainContentLayout(int layoutResource) {
		// replace main content with brand new inflated layout
		mainContentLayout.removeAllViews();
		LayoutInflater inflater = activity.getLayoutInflater();
		View layout = inflater.inflate(layoutResource, null);
		layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mainContentLayout.addView(layout);
		return layout;
	}
}
