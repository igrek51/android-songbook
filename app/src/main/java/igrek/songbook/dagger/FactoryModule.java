package igrek.songbook.dagger;


import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.activity.ActivityController;
import igrek.songbook.service.activity.AppInitializer;
import igrek.songbook.service.activity.OptionSelectDispatcher;
import igrek.songbook.service.activity.SystemKeyDispatcher;
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.chords.ChordsManager;
import igrek.songbook.service.database.PersistenceService;
import igrek.songbook.service.database.SongsDatabaseService;
import igrek.songbook.service.database.SqlQueryService;
import igrek.songbook.service.filesystem.ExternalCardService;
import igrek.songbook.service.filesystem.FilesystemService;
import igrek.songbook.service.filetree.FileTreeManager;
import igrek.songbook.service.filetree.ScrollPosBuffer;
import igrek.songbook.service.info.UiInfoService;
import igrek.songbook.service.info.UiResourceService;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.layout.about.AboutLayoutController;
import igrek.songbook.service.layout.contact.ContactLayoutController;
import igrek.songbook.service.layout.help.HelpLayoutController;
import igrek.songbook.service.layout.search.SongSearchController;
import igrek.songbook.service.layout.songpreview.SongPreviewController;
import igrek.songbook.service.layout.songtree.HomePathService;
import igrek.songbook.service.layout.songtree.SongTreeController;
import igrek.songbook.service.navmenu.NavigationMenuController;
import igrek.songbook.service.preferences.PreferencesService;
import igrek.songbook.service.transpose.ChordsTransposer;
import igrek.songbook.service.window.SoftKeyboardService;
import igrek.songbook.service.window.WindowManagerService;
import igrek.songbook.view.songpreview.quickmenu.QuickMenu;

/**
 * Module with providers. These classes can be injected
 */
@Module
public class FactoryModule {
	
	private AppCompatActivity activity;
	
	public FactoryModule(AppCompatActivity activity) {
		this.activity = activity;
	}
	
	@Provides
	protected Context provideContext() {
		return activity.getApplicationContext();
	}
	
	@Provides
	protected Activity provideActivity() {
		return activity;
	}
	
	@Provides
	protected AppCompatActivity provideAppCompatActivity() {
		return activity;
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
	protected UiResourceService provideUIResourceService() {
		return new UiResourceService();
	}
	
	@Provides
	@Singleton
	protected UiInfoService provideUserInfoService() {
		return new UiInfoService();
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
	protected SongTreeController provideSongSelectionController() {
		return new SongTreeController();
	}
	
	@Provides
	@Singleton
	protected SongPreviewController provideSongPreviewController() {
		return new SongPreviewController();
	}
	
	@Provides
	@Singleton
	protected QuickMenu provideQuickMenu() {
		return new QuickMenu();
	}
	
	@Provides
	@Singleton
	protected HomePathService provideHomePathService() {
		return new HomePathService();
	}
	
	@Provides
	@Singleton
	protected NavigationMenuController provideNavigationMenuController() {
		return new NavigationMenuController();
	}
	
	@Provides
	@Singleton
	protected SongSearchController provideSongSearchController() {
		return new SongSearchController();
	}
	
	@Provides
	@Singleton
	protected AboutLayoutController provideAboutLayoutController() {
		return new AboutLayoutController();
	}
	
	@Provides
	@Singleton
	protected ContactLayoutController provideContactLayoutController() {
		return new ContactLayoutController();
	}
	
	@Provides
	@Singleton
	protected HelpLayoutController provideHelpLayoutController() {
		return new HelpLayoutController();
	}
	
	@Provides
	@Singleton
	protected SongsDatabaseService provideSongsDbService() {
		return new SongsDatabaseService();
	}
	
	@Provides
	@Singleton
	protected SqlQueryService provideSqlQueryService() {
		return new SqlQueryService();
	}
	
	@Provides
	@Singleton
	protected PersistenceService providePersistenceService() {
		return new PersistenceService();
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
