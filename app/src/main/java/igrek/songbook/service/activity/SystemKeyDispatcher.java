package igrek.songbook.service.activity;

import android.os.Handler;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.filetree.FileTreeManager;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.layout.LayoutState;
import igrek.songbook.service.layout.songselection.SongSelectionController;
import igrek.songbook.service.window.WindowManagerService;
import igrek.songbook.view.canvas.quickmenu.QuickMenu;

public class SystemKeyDispatcher {
	
	@Inject
	LayoutController layoutController;
	@Inject
	SongSelectionController songSelectionController;
	@Inject
	QuickMenu quickMenu;
	@Inject
	WindowManagerService windowManagerService;
	@Inject
	FileTreeManager fileTreeManager;
	@Inject
	AutoscrollService autoscrollService;
	
	public SystemKeyDispatcher() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public boolean onKeyBack() {
		if (layoutController.isState(LayoutState.SONG_LIST)) {
			songSelectionController.onToolbarBackClickedEvent();
		} else if (layoutController.isState(LayoutState.SONG_PREVIEW)) {
			if (quickMenu.isVisible()) {
				quickMenu.onShowQuickMenuEvent(false);
			} else {
				autoscrollService.onAutoscrollStopEvent();
				
				layoutController.setState(LayoutState.SONG_LIST);
				layoutController.showFileList();
				
				windowManagerService.dontKeepScreenOn();
				
				new Handler().post(() -> songSelectionController.restoreScrollPosition(fileTreeManager
						.getCurrentPath()));
			}
		}
		return true;
	}
	
	public boolean onKeyMenu() {
		return false;
	}
}
