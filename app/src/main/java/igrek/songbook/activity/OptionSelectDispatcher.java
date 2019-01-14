package igrek.songbook.activity;

import android.util.SparseArray;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.errorcheck.SafeExecutor;

public class OptionSelectDispatcher {
	
	private SparseArray<Runnable> optionActions = new SparseArray<>();
	
	public OptionSelectDispatcher() {
		DaggerIoc.getFactoryComponent().inject(this);
		initOptionActionsMap();
	}
	
	private void initOptionActionsMap() {
	}
	
	public boolean optionsSelect(int id) {
		if (optionActions.get(id) != null) {
			Runnable action = optionActions.get(id);
			new SafeExecutor().execute(action);
			return true;
		}
		return false;
	}
}
