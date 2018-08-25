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
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.chords.ChordsManager;
import igrek.songbook.service.filesystem.ExternalCardService;
import igrek.songbook.service.filesystem.FilesystemService;
import igrek.songbook.service.filetree.FileTreeManager;
import igrek.songbook.service.filetree.ScrollPosBuffer;
import igrek.songbook.service.info.UIResourceService;
import igrek.songbook.service.info.UserInfoService;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.layout.songpreview.SongPreviewController;
import igrek.songbook.service.layout.songselection.SongSelectionController;
import igrek.songbook.service.preferences.PreferencesService;
import igrek.songbook.service.transpose.ChordsTransposer;
import igrek.songbook.service.window.SoftKeyboardService;
import igrek.songbook.service.window.WindowManagerService;

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
	protected WindowManagerService provideScreenService() {
		return new WindowManagerService();
	}
	
	@Provides
	@Singleton
	protected UIResourceService provideUIResourceService() {
		return new UIResourceService();
	}
	
	@Provides
	@Singleton
	protected UserInfoService provideUserInfoService() {
		return new UserInfoService();
	}
	
	@Provides
	@Singleton
	protected AutoscrollService provideAutoscrollService() {
		return new AutoscrollService();
	}
	
	@Provides
	@Singleton
	protected ChordsManager provideChordsManager() {
		return new ChordsManager();
	}
	
	@Provides
	@Singleton
	protected FileTreeManager provideFileTreeManager() {
		return new FileTreeManager();
	}
	
	@Provides
	@Singleton
	protected PreferencesService providePreferencesService() {
		return new PreferencesService();
	}
	
	@Provides
	@Singleton
	protected ChordsTransposer provideChordsTransposer() {
		return new ChordsTransposer();
	}
	
	@Provides
	@Singleton
	protected ScrollPosBuffer provideScrollPosBuffer() {
		return new ScrollPosBuffer();
	}
	
	@Provides
	@Singleton
	protected LayoutController provideLayoutController() {
		return new LayoutController();
	}
	
	@Provides
	@Singleton
	protected SoftKeyboardService provideSoftKeyboardService() {
		return new SoftKeyboardService();
	}
	
	@Provides
	@Singleton
	protected SongSelectionController provideSongSelectionController() {
		return new SongSelectionController();
	}
	
	@Provides
	@Singleton
	protected SongPreviewController provideSongPreviewController() {
		return new SongPreviewController();
	}
	
	/*
	 * Empty service pattern:
	@Provides
	@Singleton
	protected  provide() {
		return new ();
	}
	
	 */
	
}
