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
import igrek.songbook.service.info.UIInfoService;
import igrek.songbook.service.info.UIResourceService;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.layout.songpreview.SongPreviewController;
import igrek.songbook.service.layout.songselection.HomePathService;
import igrek.songbook.service.layout.songselection.SongSelectionController;
import igrek.songbook.service.preferences.PreferencesService;
import igrek.songbook.service.transpose.ChordsTransposer;
import igrek.songbook.service.window.SoftKeyboardService;
import igrek.songbook.service.window.WindowManagerService;
import igrek.songbook.view.songpreview.CanvasGraphics;
import igrek.songbook.view.songpreview.quickmenu.QuickMenu;
import igrek.songbook.view.songselection.FileListView;

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
	
	void inject(UIResourceService there);
	
	void inject(UIInfoService there);
	
	void inject(AutoscrollService there);
	
	void inject(ChordsManager there);
	
	void inject(FileTreeManager there);
	
	void inject(PreferencesService there);
	
	void inject(ChordsTransposer there);
	
	void inject(SoftKeyboardService there);
	
	void inject(LayoutController there);
	
	void inject(SongSelectionController there);
	
	void inject(SongPreviewController there);
	
	void inject(ScrollPosBuffer there);
	
	void inject(SystemKeyDispatcher there);
	
	void inject(HomePathService there);
	
	
	void inject(UIErrorHandler there);
	
	void inject(FileListView there);
	
	void inject(SafeExecutor there);
	
	void inject(QuickMenu there);
	
	void inject(CanvasGraphics there);
	
}