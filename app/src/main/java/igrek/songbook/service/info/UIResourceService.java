package igrek.songbook.service.info;

import android.app.Activity;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;

public class UIResourceService {
	
	@Inject
	Activity activity;
	
	public UIResourceService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public String resString(int resourceId) {
		return activity.getResources().getString(resourceId);
	}
	
}
