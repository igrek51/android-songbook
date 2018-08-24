package igrek.songbook.service.screen;

import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;

public class ScreenService {
	
	@Inject
	AppCompatActivity activity;
	
	public ScreenService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	private static final int FULLSCREEN_FLAG = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
	
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
		activity.getWindow().setFlags(FULLSCREEN_FLAG, FULLSCREEN_FLAG);
	}
}
