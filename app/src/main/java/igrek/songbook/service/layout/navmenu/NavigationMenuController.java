package igrek.songbook.service.layout.navmenu;

import android.app.Activity;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.errorcheck.SafeExecutor;
import igrek.songbook.service.info.UiInfoService;

public class NavigationMenuController {
	
	private DrawerLayout drawerLayout;
	private NavigationView navigationView;
	private Map<Integer, Runnable> actionsMap = new HashMap<>();
	private Logger logger = LoggerFactory.getLogger();
	
	@Inject
	Activity activity;
	
	@Inject
	UiInfoService uiInfoService;
	
	public NavigationMenuController() {
		DaggerIoc.getFactoryComponent().inject(this);
		initOptionActionsMap();
	}
	
	private void initOptionActionsMap() {
		actionsMap.put(R.id.nav_songs_list, () -> uiInfoService.showToast("not implemented yet"));
		actionsMap.put(R.id.nav_search, () -> uiInfoService.showToast("not implemented yet"));
		actionsMap.put(R.id.nav_update_db, () -> uiInfoService.showToast("not implemented yet"));
		actionsMap.put(R.id.nav_import_song, () -> uiInfoService.showToast("not implemented yet"));
		actionsMap.put(R.id.nav_settings, () -> uiInfoService.showToast("not implemented yet"));
		actionsMap.put(R.id.nav_help, () -> uiInfoService.showToast("not implemented yet"));
		actionsMap.put(R.id.nav_about, () -> uiInfoService.showToast("not implemented yet"));
		actionsMap.put(R.id.nav_exit, () -> uiInfoService.showToast("not implemented yet"));
	}
	
	public void init() {
		drawerLayout = activity.findViewById(R.id.drawer_layout);
		navigationView = activity.findViewById(R.id.nav_view);
		
		navigationView.setNavigationItemSelectedListener(menuItem -> {
			// set item as selected to persist highlight
			menuItem.setChecked(true);
			drawerLayout.closeDrawers();
			int id = menuItem.getItemId();
			if (actionsMap.containsKey(id)) {
				Runnable action = actionsMap.get(id);
				new SafeExecutor().execute(action);
			} else {
				logger.warn("unknown navigation item has been selected.");
			}
			return true;
		});
	}
	
	public void navDrawerShow() {
		drawerLayout.openDrawer(GravityCompat.START);
		// unhighlight all menu items
		for(int id = 0; id < navigationView.getMenu().size(); id++)
			navigationView.getMenu().getItem(id).setChecked(false);
	}
	
}
