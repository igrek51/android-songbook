package igrek.songbook.system;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.layout.LayoutController;

public class SystemKeyDispatcher {
	
	@Inject
	LayoutController layoutController;
	
	public SystemKeyDispatcher() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public boolean onKeyBack() {
		layoutController.onBackClicked();
		return true;
	}
	
	public boolean onKeyMenu() {
		return false;
	}
}
