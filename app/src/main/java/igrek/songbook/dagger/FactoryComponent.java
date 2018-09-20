package igrek.songbook.dagger;

import javax.inject.Singleton;

import dagger.Component;
import igrek.songbook.activity.ActivityController;
import igrek.songbook.activity.AppInitializer;
import igrek.songbook.activity.MainActivity;
import igrek.songbook.activity.OptionSelectDispatcher;
import igrek.songbook.info.UiInfoService;
import igrek.songbook.info.UiResourceService;
import igrek.songbook.info.errorcheck.SafeExecutor;
import igrek.songbook.info.errorcheck.UIErrorHandler;
import igrek.songbook.layout.LayoutController;
import igrek.songbook.layout.about.AboutLayoutController;
import igrek.songbook.layout.about.HelpLayoutController;
import igrek.songbook.layout.about.secret.SecretUnlockerService;
import igrek.songbook.layout.contact.ContactLayoutController;
import igrek.songbook.layout.contact.SendFeedbackService;
import igrek.songbook.layout.navigation.NavigationMenuController;
import igrek.songbook.layout.settings.SettingsLayoutController;
import igrek.songbook.layout.songpreview.LyricsManager;
import igrek.songbook.layout.songpreview.SongDetailsService;
import igrek.songbook.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.layout.songpreview.autoscroll.AutoscrollService;
import igrek.songbook.layout.songpreview.transpose.ChordsTransposerManager;
import igrek.songbook.layout.songpreview.render.SongPreview;
import igrek.songbook.layout.songpreview.quickmenu.QuickMenuAutoscroll;
import igrek.songbook.layout.songpreview.quickmenu.QuickMenuTranspose;
import igrek.songbook.layout.songsearch.SongSearchLayoutController;
import igrek.songbook.layout.songselection.SongListView;
import igrek.songbook.layout.songtree.ScrollPosBuffer;
import igrek.songbook.layout.songtree.SongTreeLayoutController;
import igrek.songbook.layout.songtree.SongTreeWalker;
import igrek.songbook.layout.view.ButtonClickEffect;
import igrek.songbook.persistence.LocalDatabaseService;
import igrek.songbook.persistence.SongsDbRepository;
import igrek.songbook.persistence.SqlQueryService;
import igrek.songbook.persistence.preferences.PreferencesService;
import igrek.songbook.system.PackageInfoService;
import igrek.songbook.system.PermissionService;
import igrek.songbook.system.SoftKeyboardService;
import igrek.songbook.system.SystemKeyDispatcher;
import igrek.songbook.system.WindowManagerService;
import igrek.songbook.system.filesystem.ExternalCardService;
import igrek.songbook.system.filesystem.FilesystemService;

/**
 * Dagger will be injecting to those classes
 */
@Singleton
@Component(modules = {FactoryModule.class})
public interface FactoryComponent {
	
	void inject(MainActivity there);
	
	/* Services */
	void inject(ExternalCardService there);
	
	void inject(FilesystemService there);
	
	void inject(AppInitializer there);
	
	void inject(ActivityController there);
	
	void inject(WindowManagerService there);
	
	void inject(OptionSelectDispatcher there);
	
	void inject(UiResourceService there);
	
	void inject(UiInfoService there);
	
	void inject(AutoscrollService there);
	
	void inject(LyricsManager there);
	
	void inject(SongTreeWalker there);
	
	void inject(PreferencesService there);
	
	void inject(ChordsTransposerManager there);
	
	void inject(SoftKeyboardService there);
	
	void inject(LayoutController there);
	
	void inject(SongTreeLayoutController there);
	
	void inject(SongPreviewLayoutController there);
	
	void inject(ScrollPosBuffer there);
	
	void inject(SystemKeyDispatcher there);
	
	void inject(NavigationMenuController there);
	
	void inject(SongSearchLayoutController there);
	
	void inject(AboutLayoutController there);
	
	void inject(ContactLayoutController there);
	
	void inject(HelpLayoutController there);
	
	void inject(LocalDatabaseService there);
	
	void inject(SqlQueryService there);
	
	void inject(SongsDbRepository there);
	
	void inject(PermissionService there);
	
	void inject(SecretUnlockerService there);
	
	void inject(PackageInfoService there);
	
	void inject(SettingsLayoutController there);
	
	void inject(SongDetailsService there);
	
	void inject(SendFeedbackService there);
	
	
	void inject(QuickMenuAutoscroll there);
	
	void inject(UIErrorHandler there);
	
	void inject(SongListView there);
	
	void inject(SafeExecutor there);
	
	void inject(QuickMenuTranspose there);
	
	void inject(SongPreview there);
	
	void inject(ButtonClickEffect there);
	
}