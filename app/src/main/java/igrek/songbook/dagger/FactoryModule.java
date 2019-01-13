package igrek.songbook.dagger;


import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import igrek.songbook.about.AboutLayoutController;
import igrek.songbook.about.HelpLayoutController;
import igrek.songbook.about.secret.SecretUnlockerService;
import igrek.songbook.activity.ActivityController;
import igrek.songbook.activity.AppInitializer;
import igrek.songbook.activity.OptionSelectDispatcher;
import igrek.songbook.contact.ContactLayoutController;
import igrek.songbook.contact.SendFeedbackService;
import igrek.songbook.custom.CustomSongEditLayoutController;
import igrek.songbook.custom.CustomSongService;
import igrek.songbook.custom.CustomSongsLayoutController;
import igrek.songbook.custom.SongImportFileChooser;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;
import igrek.songbook.layout.LayoutController;
import igrek.songbook.layout.navigation.NavigationMenuController;
import igrek.songbook.persistence.CustomSongsDao;
import igrek.songbook.persistence.FavouriteSongsDao;
import igrek.songbook.persistence.LocalDbService;
import igrek.songbook.persistence.SongsDao;
import igrek.songbook.persistence.SongsRepository;
import igrek.songbook.persistence.SongsUpdater;
import igrek.songbook.persistence.UnlockedSongsDao;
import igrek.songbook.persistence.migration.DatabaseMigrator;
import igrek.songbook.settings.SettingsLayoutController;
import igrek.songbook.settings.chordsnotation.ChordsNotationService;
import igrek.songbook.settings.language.AppLanguageService;
import igrek.songbook.settings.preferences.PreferencesService;
import igrek.songbook.settings.preferences.PreferencesUpdater;
import igrek.songbook.songpreview.LyricsManager;
import igrek.songbook.songpreview.SongDetailsService;
import igrek.songbook.songpreview.SongPreviewLayoutController;
import igrek.songbook.songpreview.autoscroll.AutoscrollService;
import igrek.songbook.songpreview.quickmenu.QuickMenuAutoscroll;
import igrek.songbook.songpreview.quickmenu.QuickMenuTranspose;
import igrek.songbook.songpreview.theme.LyricsThemeService;
import igrek.songbook.songpreview.transpose.ChordsTransposerManager;
import igrek.songbook.songselection.favourite.FavouriteSongsRepository;
import igrek.songbook.songselection.favourite.FavouritesLayoutController;
import igrek.songbook.songselection.random.RandomSongOpener;
import igrek.songbook.songselection.songsearch.SongSearchLayoutController;
import igrek.songbook.songselection.songtree.ScrollPosBuffer;
import igrek.songbook.songselection.songtree.SongTreeLayoutController;
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
	
	
	@Provides
	@Singleton
	protected CustomSongService provideSongImportService() {
		return new CustomSongService();
	}
	
	
	@Provides
	@Singleton
	protected CustomSongEditLayoutController provideEditImportSongLayoutController() {
		return new CustomSongEditLayoutController();
	}
	
	@Provides
	@Singleton
	protected SongsUpdater provideSongsUpdater() {
		return new SongsUpdater();
	}
	
	@Provides
	@Singleton
	protected RandomSongOpener provideRandomSongSelector() {
		return new RandomSongOpener();
	}
	
	@Provides
	@Singleton
	protected DatabaseMigrator provideDatabaseMigrator() {
		return new DatabaseMigrator();
	}
	
	@Provides
	@Singleton
	protected FavouriteSongsDao provideFavouriteSongsDao() {
		return new FavouriteSongsDao();
	}
	
	@Provides
	@Singleton
	protected AppLanguageService provideAppLanguageService() {
		return new AppLanguageService();
	}
	
	@Provides
	@Singleton
	protected FavouriteSongsRepository provideFavouriteSongService() {
		return new FavouriteSongsRepository();
	}
	
	@Provides
	@Singleton
	protected FavouritesLayoutController provideFavouritesLayoutController() {
		return new FavouritesLayoutController();
	}
	
	@Provides
	@Singleton
	protected ChordsNotationService provideChordsNotationService() {
		return new ChordsNotationService();
	}
	
	@Provides
	@Singleton
	protected PreferencesUpdater providePreferencesUpdater() {
		return new PreferencesUpdater();
	}
	
	@Provides
	@Singleton
	protected LyricsThemeService provideLyricsThemeService() {
		return new LyricsThemeService();
	}
	
	@Provides
	@Singleton
	protected CustomSongsLayoutController provideCustomSongsLayoutController() {
		return new CustomSongsLayoutController();
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
