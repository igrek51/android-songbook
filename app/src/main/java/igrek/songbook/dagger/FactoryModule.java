package igrek.songbook.dagger;


import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.activity.ActivityController;
import igrek.songbook.activity.AppInitializer;
import igrek.songbook.activity.OptionSelectDispatcher;
import igrek.songbook.chords.autoscroll.AutoscrollService;
import igrek.songbook.chords.LyricsManager;
import igrek.songbook.chords.transpose.ChordsTransposerManager;
import igrek.songbook.filesystem.ExternalCardService;
import igrek.songbook.filesystem.FilesystemService;
import igrek.songbook.filesystem.PermissionService;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.layout.LayoutController;
import igrek.songbook.about.AboutLayoutController;
import igrek.songbook.contact.ContactLayoutController;
import igrek.songbook.contact.SendFeedbackService;
import igrek.songbook.about.HelpLayoutController;
import igrek.songbook.songlist.search.SongSearchLayoutController;
import igrek.songbook.settings.SettingsLayoutController;
import igrek.songbook.chords.SongDetailsService;
import igrek.songbook.chords.SongPreviewLayoutController;
import igrek.songbook.songlist.tree.SongTreeLayoutController;
import igrek.songbook.layout.navigation.NavigationMenuController;
import igrek.songbook.persistence.SongsDbRepository;
import igrek.songbook.persistence.LocalDatabaseService;
import igrek.songbook.persistence.SqlQueryService;
import igrek.songbook.settings.preferences.PreferencesService;
import igrek.songbook.about.secret.SecretUnlockerService;
import igrek.songbook.songlist.tree.ScrollPosBuffer;
import igrek.songbook.songlist.tree.SongTreeWalker;
import igrek.songbook.system.PackageInfoService;
import igrek.songbook.system.SoftKeyboardService;
import igrek.songbook.system.SystemKeyDispatcher;
import igrek.songbook.system.WindowManagerService;
import igrek.songbook.chords.view.quickmenu.QuickMenu;

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
	
	@Provides
	@Singleton
	protected SongDetailsService provideSongDetailsService() {
		return new SongDetailsService();
	}
	
	@Provides
	@Singleton
	protected SendFeedbackService provideSendFeedbackService() {
		return new SendFeedbackService();
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
