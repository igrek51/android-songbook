package igrek.songbook.dagger;

import javax.inject.Singleton;

import dagger.Component;
import igrek.songbook.activity.MainActivity;
import igrek.songbook.service.activity.ActivityController;
import igrek.songbook.service.activity.AppInitializer;
import igrek.songbook.service.activity.OptionSelectDispatcher;
import igrek.songbook.service.activity.SystemKeyDispatcher;
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.chords.ChordsManager;
import igrek.songbook.service.errorcheck.SafeExecutor;
import igrek.songbook.service.errorcheck.UIErrorHandler;
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
import igrek.songbook.service.persistence.PersistenceService;
import igrek.songbook.service.persistence.SongsDbRepository;
import igrek.songbook.service.persistence.database.LocalDatabaseService;
import igrek.songbook.service.persistence.database.SqlQueryService;
import igrek.songbook.service.preferences.PreferencesService;
import igrek.songbook.service.transpose.ChordsTransposer;
import igrek.songbook.service.window.SoftKeyboardService;
import igrek.songbook.service.window.WindowManagerService;
import igrek.songbook.view.songpreview.CanvasGraphics;
import igrek.songbook.view.songpreview.quickmenu.QuickMenu;
import igrek.songbook.view.songselection.SongListView;

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
	
	void inject(ChordsManager there);
	
	void inject(FileTreeManager there);
	
	void inject(PreferencesService there);
	
	void inject(ChordsTransposer there);
	
	void inject(SoftKeyboardService there);
	
	void inject(LayoutController there);
	
	void inject(SongTreeController there);
	
	void inject(SongPreviewController there);
	
	void inject(ScrollPosBuffer there);
	
	void inject(SystemKeyDispatcher there);
	
	void inject(HomePathService there);
	
	void inject(NavigationMenuController there);
	
	void inject(SongSearchController there);
	
	void inject(AboutLayoutController there);
	
	void inject(ContactLayoutController there);
	
	void inject(HelpLayoutController there);
	
	void inject(LocalDatabaseService there);
	
	void inject(SqlQueryService there);
	
	void inject(PersistenceService there);
	
	void inject(SongsDbRepository there);
	
	
	void inject(UIErrorHandler there);
	
	void inject(SongListView there);
	
	void inject(SafeExecutor there);
	
	void inject(QuickMenu there);
	
	void inject(CanvasGraphics there);
	
}