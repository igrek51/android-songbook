package igrek.songbook.dagger;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import igrek.songbook.MainApplication;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.activity.ActivityController;
import igrek.songbook.service.activity.AppInitializer;
import igrek.songbook.service.activity.OptionSelectDispatcher;
import igrek.songbook.service.activity.SystemKeyDispatcher;
import igrek.songbook.service.filesystem.ExternalCardService;
import igrek.songbook.service.filesystem.FilesystemService;
import igrek.songbook.service.screen.ScreenService;
import igrek.songbook.service.state.AppStateService;

/**
 * Module with providers. These classes can be injected
 */
@Module
public class FactoryModule {
	
	private MainApplication application;
	
	public FactoryModule(MainApplication application) {
		this.application = application;
	}
	
	@Provides
	protected Application provideApplication() {
		return application;
	}
	
	@Provides
	protected Context provideContext() {
		return application.getApplicationContext();
	}
	
	@Provides
	protected Activity provideActivity() {
		return application.getCurrentActivity();
	}
	
	@Provides
	protected AppCompatActivity provideAppCompatActivity() {
		return (AppCompatActivity) provideActivity();
	}
	
	@Provides
	protected Logger provideLogger() {
		return LoggerFactory.getLogger();
	}
	
	/* Services */
	
	@Provides
	@Singleton
	protected FilesystemService provideFilesystemService() {
		return new FilesystemService();
	}
	
	@Provides
	@Singleton
	protected ActivityController provideActivityController() {
		return new ActivityController();
	}
	
	@Provides
	@Singleton
	protected AppInitializer provideAppInitializer() {
		return new AppInitializer();
	}
	
	@Provides
	@Singleton
	protected OptionSelectDispatcher provideOptionSelectDispatcher() {
		return new OptionSelectDispatcher();
	}
	
	@Provides
	@Singleton
	protected SystemKeyDispatcher provideSystemKeyDispatcher() {
		return new SystemKeyDispatcher();
	}
	
	@Provides
	@Singleton
	protected ExternalCardService provideExternalCardService() {
		return new ExternalCardService();
	}
	
	@Provides
	@Singleton
	protected ScreenService provideScreenService() {
		return new ScreenService();
	}
	
	@Provides
	@Singleton
	protected AppStateService provideAppStateService() {
		return new AppStateService();
	}
	
	/*
	empty service pattern:
	@Provides
	@Singleton
	protected  provide() {
		return new ();
	}
	 */
	
}
