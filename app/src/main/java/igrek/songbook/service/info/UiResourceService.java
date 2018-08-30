package igrek.songbook.service.info;

import android.app.Activity;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;

public class UiResourceService {
	
	@Inject
	Activity activity;
	
	public UiResourceService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public String resString(int resourceId) {
		return activity.getResources().getString(resourceId);
	}
	
}
