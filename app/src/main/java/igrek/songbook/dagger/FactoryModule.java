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
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.chords.LyricsManager;
import igrek.songbook.service.chords.transpose.ChordsTransposerManager;
import igrek.songbook.service.filesystem.ExternalCardService;
import igrek.songbook.service.filesystem.FilesystemService;
import igrek.songbook.service.filesystem.PermissionService;
import igrek.songbook.service.info.UiInfoService;
import igrek.songbook.service.info.UiResourceService;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.layout.about.AboutLayoutController;
import igrek.songbook.service.layout.contact.ContactLayoutController;
import igrek.songbook.service.layout.help.HelpLayoutController;
import igrek.songbook.service.layout.search.SongSearchLayoutController;
import igrek.songbook.service.layout.settings.SettingsLayoutController;
import igrek.songbook.service.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.service.layout.songtree.SongTreeLayoutController;
import igrek.songbook.service.navmenu.NavigationMenuController;
import igrek.songbook.service.persistence.SongsDbRepository;
import igrek.songbook.service.persistence.database.LocalDatabaseService;
import igrek.songbook.service.persistence.database.SqlQueryService;
import igrek.songbook.service.preferences.PreferencesService;
import igrek.songbook.service.secret.SecretUnlockerService;
import igrek.songbook.service.songtree.ScrollPosBuffer;
import igrek.songbook.service.songtree.SongTreeWalker;
import igrek.songbook.service.system.PackageInfoService;
import igrek.songbook.service.system.SoftKeyboardService;
import igrek.songbook.service.system.SystemKeyDispatcher;
import igrek.songbook.service.system.WindowManagerService;
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
	protected LyricsManager provideChordsManager() {
		return new LyricsManager();
	}
	
	@Provides
	@Singleton
	protected SongTreeWalker provideFileTreeManager() {
		return new SongTreeWalker();
	}
	
	@Provides
	@Singleton
	protected PreferencesService providePreferencesService() {
		return new PreferencesService();
	}
	
	@Provides
	@Singleton
	protected ChordsTransposerManager provideChordsTransposerManager() {
		return new ChordsTransposerManager();
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
	protected SongTreeLayoutController provideSongSelectionController() {
		return new SongTreeLayoutController();
	}
	
	@Provides
	@Singleton
	protected SongPreviewLayoutController provideSongPreviewController() {
		return new SongPreviewLayoutController();
	}
	
	@Provides
	@Singleton
	protected QuickMenu provideQuickMenu() {
		return new QuickMenu();
	}
	
	@Provides
	@Singleton
	protected NavigationMenuController provideNavigationMenuController() {
		return new NavigationMenuController();
	}
	
	@Provides
	@Singleton
	protected SongSearchLayoutController provideSongSearchController() {
		return new SongSearchLayoutController();
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
	protected LocalDatabaseService provideSongsDbService() {
		return new LocalDatabaseService();
	}
	
	@Provides
	@Singleton
	protected SqlQueryService provideSqlQueryService() {
		return new SqlQueryService();
	}
	
	@Provides
	@Singleton
	protected SongsDbRepository provideSongsDbRepository() {
		return new SongsDbRepository();
	}
	
	@Provides
	@Singleton
	protected PermissionService providePermissionService() {
		return new PermissionService();
	}
	
	@Provides
	@Singleton
	protected SecretUnlockerService provideSecretUnlockerService() {
		return new SecretUnlockerService();
	}
	
	@Provides
	@Singleton
	protected PackageInfoService providePackageInfoService() {
		return new PackageInfoService();
	}
	
	@Provides
	@Singleton
	protected SettingsLayoutController provideSettingsLayoutController() {
		return new SettingsLayoutController();
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
