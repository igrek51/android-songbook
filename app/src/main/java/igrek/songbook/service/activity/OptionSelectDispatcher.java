package igrek.songbook.service.activity;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.service.errorcheck.SafeExecutor;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.layout.songselection.SongSelectionController;

public class OptionSelectDispatcher {
	
	@Inject
	ActivityController activityController;
	
	@Inject
	SongSelectionController songSelectionController;
	
	@Inject
	LayoutController layoutController;
	
	private Map<Integer, Runnable> optionActions = new HashMap<>();
	
	public OptionSelectDispatcher() {
		DaggerIoc.getFactoryComponent().inject(this);
		initOptionActionsMap();
	}
	
	private void initOptionActionsMap() {
		optionActions.put(R.id.action_minimize, activityController::minimize);
		optionActions.put(R.id.action_exit, activityController::quit);
		optionActions.put(R.id.action_sethomedir, songSelectionController::setHomePath);
		optionActions.put(R.id.action_ui_help, songSelectionController::showUIHelp);
	}
	
	public boolean optionsSelect(int id) {
		if (optionActions.containsKey(id)) {
			Runnable action = optionActions.get(id);
			new SafeExecutor().execute(action);
			return true;
		}
		return false;
	}
}
