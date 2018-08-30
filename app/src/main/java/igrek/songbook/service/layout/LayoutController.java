package igrek.songbook.service.layout;

import android.app.Activity;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.layout.songpreview.SongPreviewController;
import igrek.songbook.service.layout.songselection.SongSelectionController;

public class LayoutController {
	
	@Inject
	Lazy<SongSelectionController> songSelectionController;
	
	@Inject
	Lazy<SongPreviewController> songPreviewController;
	
	@Inject
	Activity activity;
	
	private DrawerLayout drawerLayout;
	private FrameLayout mainContentLayout;
	private NavigationView navigationView;
	private Logger logger = LoggerFactory.getLogger();
	
	private LayoutState state = LayoutState.SONG_LIST;
	
	public LayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void init() {
		activity.setContentView(R.layout.main_layout);
		drawerLayout = activity.findViewById(R.id.drawer_layout);
		mainContentLayout = activity.findViewById(R.id.main_content);
		navigationView = activity.findViewById(R.id.nav_view);
		
		navigationView.setNavigationItemSelectedListener(menuItem -> {
			int id = menuItem.getItemId();
			if (id == R.id.nav_about) {
				Toast.makeText(activity, "Camera is clicked", Toast.LENGTH_SHORT).show();
			}
			// set item as selected to persist highlight
			menuItem.setChecked(true);
			drawerLayout.closeDrawers();
			return true;
		});
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
	
	public void navDrawerShow() {
		drawerLayout.openDrawer(GravityCompat.START);
		// deselect all menu items
		for(int id = 0; id < navigationView.getMenu().size(); id++)
			navigationView.getMenu().getItem(id).setChecked(false);
	}
}
