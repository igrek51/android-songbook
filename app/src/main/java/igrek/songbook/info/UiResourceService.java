package igrek.songbook.info;

import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

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
	
	public String resString(int resourceId, Object... args) {
		String message = resString(resourceId);
		if (args.length > 0) {
			return String.format(message, args);
		} else {
			return message;
		}
	}
	
	@ColorInt
	public int getColor(@ColorRes int resourceId) {
		return ContextCompat.getColor(activity, resourceId);
	}
	
}
