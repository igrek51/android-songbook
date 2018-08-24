package igrek.songbook.service.activity;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;

public class OptionSelectDispatcher {
	
	@Inject
	ActivityController activityController;
	
	public OptionSelectDispatcher() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public boolean optionsSelect(int id) {
		if (id == R.id.action_minimize) {
			activityController.minimize();
			return true;
		} else if (id == R.id.action_exit) {
			activityController.quit();
			return true;
		}
		//TODO
		/* else if (id == R.id.action_home) {
			homeClicked();
			return true;
		} else if (id == R.id.action_sethomedir) {
			setHomePath();
			return true;
		} else if (id == R.id.action_ui_help) {
			showUIHelp();
			return true;
		}*/
		return false;
	}
}
