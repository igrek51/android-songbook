package igrek.songbook.service.activity;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.service.errorcheck.SafeExecutor;

public class OptionSelectDispatcher {
	
	@Inject
	ActivityController activityController;
	
	private Map<Integer, Runnable> optionActions = new HashMap<>();
	
	public OptionSelectDispatcher() {
		DaggerIoc.getFactoryComponent().inject(this);
		initOptionActionsMap();
	}
	
	private void initOptionActionsMap() {
		optionActions.put(R.id.action_minimize, activityController::minimize);
		optionActions.put(R.id.action_exit, activityController::quit);
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
