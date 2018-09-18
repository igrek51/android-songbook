package igrek.songbook.activity;

import java.util.HashMap;
import java.util.Map;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.errorcheck.SafeExecutor;

public class OptionSelectDispatcher {
	
	private Map<Integer, Runnable> optionActions = new HashMap<>();
	
	public OptionSelectDispatcher() {
		DaggerIoc.getFactoryComponent().inject(this);
		initOptionActionsMap();
	}
	
	private void initOptionActionsMap() {
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
