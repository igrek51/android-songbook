package igrek.songbook.dagger;

import javax.inject.Singleton;

import dagger.Component;
import igrek.songbook.activity.MainActivity;
import igrek.songbook.activity.ActivityController;
import igrek.songbook.activity.AppInitializer;
import igrek.songbook.activity.OptionSelectDispatcher;
import igrek.songbook.chords.autoscroll.AutoscrollService;
import igrek.songbook.chords.LyricsManager;
import igrek.songbook.chords.transpose.ChordsTransposerManager;
import igrek.songbook.errorcheck.SafeExecutor;
import igrek.songbook.errorcheck.UIErrorHandler;
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
import igrek.songbook.songlist.view.SongListView;
import igrek.songbook.chords.view.SongPreview;
import igrek.songbook.chords.view.quickmenu.QuickMenu;

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
	
	
	void inject(UIErrorHandler there);
	
	void inject(SongListView there);
	
	void inject(SafeExecutor there);
	
	void inject(QuickMenu there);
	
	void inject(SongPreview there);
	
}