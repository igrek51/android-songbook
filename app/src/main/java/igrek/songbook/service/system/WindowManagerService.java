package igrek.songbook.service.system;

import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;

public class WindowManagerService {
	
	@Inject
	AppCompatActivity activity;
	
	public WindowManagerService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void keepScreenOn(boolean set) {
		if (set) {
			activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		setShowWhenLocked(set);
	}
	
	public void hideTaskbar() {
		if (activity.getSupportActionBar() != null) {
			activity.getSupportActionBar().hide();
		}
	}
	
	public void setFullscreen(boolean set) {
		int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		if (set) {
			activity.getWindow().addFlags(flag);
		} else {
			activity.getWindow().clearFlags(flag);
		}
	}
	
	public void setShowWhenLocked(boolean set) {
		int flag = WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		if (set) {
			activity.getWindow().setFlags(flag, flag);
		} else {
			activity.getWindow().clearFlags(flag);
		}
	}
	
	public int getDpi() {
		DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
		return metrics.densityDpi;
	}
	
	public float dp2px(float dp) {
		return dp * ((float) getDpi() / DisplayMetrics.DENSITY_DEFAULT);
	}
}
