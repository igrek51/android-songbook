package igrek.songbook;

import android.app.Activity;
import android.app.Application;

import igrek.songbook.activity.CurrentActivityListener;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;

public class MainApplication extends Application {
	
	private Logger logger = LoggerFactory.getLogger();
	private CurrentActivityListener currentActivityListener = new CurrentActivityListener();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		registerActivityLifecycleCallbacks(currentActivityListener);
		
		// catch all uncaught exceptions
		Thread.UncaughtExceptionHandler defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler((thread, th) -> {
			logger.error(th);
			//pass further to OS
			defaultUEH.uncaughtException(thread, th);
		});
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		unregisterActivityLifecycleCallbacks(currentActivityListener);
	}
	
	public Activity getCurrentActivity() {
		return currentActivityListener.getCurrentActivity();
	}
}