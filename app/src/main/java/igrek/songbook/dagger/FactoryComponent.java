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
import igrek.songbook.layout.edit.EditSongLayoutController;
import igrek.songbook.layout.edit.SongEditService;
import igrek.songbook.layout.edit.SongImportFileChooser;
import igrek.songbook.layout.navigation.NavigationMenuController;
import igrek.songbook.layout.settings.SettingsLayoutController;
import igrek.songbook.layout.songpreview.LyricsManager;
import igrek.songbook.layout.songpreview.SongDetailsService;
import igrek.songbook.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.layout.songpreview.autoscroll.AutoscrollService;
import igrek.songbook.layout.songpreview.quickmenu.QuickMenuAutoscroll;
import igrek.songbook.layout.songpreview.quickmenu.QuickMenuTranspose;
import igrek.songbook.layout.songpreview.renderer.SongPreview;
import igrek.songbook.layout.songpreview.transpose.ChordsTransposerManager;
import igrek.songbook.layout.songsearch.SongSearchLayoutController;
import igrek.songbook.layout.songselection.SongListItemAdapter;
import igrek.songbook.layout.songselection.SongListView;
import igrek.songbook.layout.songtree.ScrollPosBuffer;
import igrek.songbook.layout.songtree.SongTreeLayoutController;
import igrek.songbook.layout.view.ButtonClickEffect;
import igrek.songbook.persistence.CustomSongsDao;
import igrek.songbook.persistence.LocalDbService;
import igrek.songbook.persistence.SongsDao;
import igrek.songbook.persistence.SongsRepository;
import igrek.songbook.persistence.SongsUpdater;
import igrek.songbook.persistence.UnlockedSongsDao;
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
	
	void inject(LocalDbService there);
	
	void inject(SongsRepository there);
	
	void inject(PermissionService there);
	
	void inject(SecretUnlockerService there);
	
	void inject(PackageInfoService there);
	
	void inject(SettingsLayoutController there);
	
	void inject(SongDetailsService there);
	
	void inject(SendFeedbackService there);
	
	void inject(SongImportFileChooser there);
	
	void inject(UnlockedSongsDao there);
	
	void inject(SongsDao there);
	
	void inject(CustomSongsDao there);
	
	void inject(SongEditService there);
	
	void inject(EditSongLayoutController there);
	
	void inject(SongsUpdater there);
	
	
	void inject(SongListItemAdapter there);
	
	void inject(QuickMenuAutoscroll there);
	
	void inject(UIErrorHandler there);
	
	void inject(SongListView there);
	
	void inject(SafeExecutor there);
	
	void inject(QuickMenuTranspose there);
	
	void inject(SongPreview there);
	
	void inject(ButtonClickEffect there);
	
}