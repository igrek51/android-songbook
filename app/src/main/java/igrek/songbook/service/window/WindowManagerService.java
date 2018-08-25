package igrek.songbook.service.window;

import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;

public class WindowManagerService {
	
	@Inject
	AppCompatActivity activity;
	
	public WindowManagerService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void keepScreenOn() {
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	public void keepScreenOff() {
		activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	public void hideTaskbar() {
		if (activity.getSupportActionBar() != null) {
			activity.getSupportActionBar().hide();
		}
	}
	
	public void setFullscreen() {
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	public void setFullscreenLocked(boolean set) {
		int fullscreen_flag = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		if (set) {
			activity.getWindow().setFlags(fullscreen_flag, fullscreen_flag);
		} else {
			activity.getWindow().clearFlags(fullscreen_flag);
		}
	}
}
