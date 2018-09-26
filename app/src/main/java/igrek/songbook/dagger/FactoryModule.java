package igrek.songbook.dagger;


import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import igrek.songbook.activity.ActivityController;
import igrek.songbook.activity.AppInitializer;
import igrek.songbook.activity.OptionSelectDispatcher;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.LayoutController;
import igrek.songbook.layout.about.AboutLayoutController;
import igrek.songbook.layout.about.HelpLayoutController;
import igrek.songbook.layout.about.secret.SecretUnlockerService;
import igrek.songbook.layout.contact.ContactLayoutController;
import igrek.songbook.layout.contact.SendFeedbackService;
import igrek.songbook.layout.navigation.NavigationMenuController;
import igrek.songbook.layout.settings.SettingsLayoutController;
import igrek.songbook.layout.songimport.SongImportFileChooser;
import igrek.songbook.layout.songpreview.LyricsManager;
import igrek.songbook.layout.songpreview.SongDetailsService;
import igrek.songbook.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.layout.songpreview.autoscroll.AutoscrollService;
import igrek.songbook.layout.songpreview.quickmenu.QuickMenuAutoscroll;
import igrek.songbook.layout.songpreview.quickmenu.QuickMenuTranspose;
import igrek.songbook.layout.songpreview.transpose.ChordsTransposerManager;
import igrek.songbook.layout.songsearch.SongSearchLayoutController;
import igrek.songbook.layout.songtree.ScrollPosBuffer;
import igrek.songbook.layout.songtree.SongTreeLayoutController;
import igrek.songbook.layout.songtree.SongTreeWalker;
import igrek.songbook.persistence.CustomSongsDao;
import igrek.songbook.persistence.LocalDbService;
import igrek.songbook.persistence.SongsDao;
import igrek.songbook.persistence.SongsRepository;
import igrek.songbook.persistence.UnlockedSongsDao;
import igrek.songbook.persistence.preferences.PreferencesService;
import igrek.songbook.system.PackageInfoService;
import igrek.songbook.system.PermissionService;
import igrek.songbook.system.SoftKeyboardService;
import igrek.songbook.system.SystemKeyDispatcher;
import igrek.songbook.system.WindowManagerService;
import igrek.songbook.system.filesystem.ExternalCardService;
import igrek.songbook.system.filesystem.FilesystemService;
import okhttp3.OkHttpClient;

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
	protected QuickMenuTranspose provideQuickMenu() {
		return new QuickMenuTranspose();
	}
	
	@Provides
	@Singleton
	protected QuickMenuAutoscroll provideQuickMenuAutoscroll() {
		return new QuickMenuAutoscroll();
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
	protected LocalDbService provideSongsDbService() {
		return new LocalDbService();
	}
	
	@Provides
	@Singleton
	protected SongsRepository provideSongsRepository() {
		return new SongsRepository();
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
	
	@Provides
	@Singleton
	protected SongImportFileChooser provideSongImportFileChooser() {
		return new SongImportFileChooser();
	}
	
	@Provides
	@Singleton
	protected OkHttpClient provideOkHttpClient() {
		return new OkHttpClient();
	}
	
	@Provides
	@Singleton
	protected SongsDao provideSongsDao() {
		return new SongsDao();
	}
	
	@Provides
	@Singleton
	protected CustomSongsDao provideCustomSongsDao() {
		return new CustomSongsDao();
	}
	
	@Provides
	@Singleton
	protected UnlockedSongsDao provideUnlockedSongsDao() {
		return new UnlockedSongsDao();
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
