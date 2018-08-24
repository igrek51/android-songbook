package igrek.songbook.service.activity;

public class SystemKeyDispatcher {
	
	public boolean onKeyBack() {
		//TODO
//		if (state == AppState.FILE_LIST) {
//			goUp();
//		} else if (state == AppState.FILE_CONTENT) {
//
//			QuickMenu quickMenu = AppController.getService(QuickMenu.class);
//			if (quickMenu.isVisible()) {
//				AppController.sendEvent(new ShowQuickMenuEvent(false));
//			} else {
//
//				AppController.sendEvent(new AutoscrollStopEvent());
//				state = AppState.FILE_LIST;
//				gui.showFileList(fileTreeManager.getCurrentDirName(), fileTreeManager.getItems());
//
//				keepScreenOff(activity);
//
//				new Handler().post(new Runnable() {
//					@Override
//					public void run() {
//						restoreScrollPosition(fileTreeManager.getCurrentPath());
//					}
//				});
//
//			}
//
//		}
		return true;
	}
	
	public boolean onKeyMenu() {
		return false;
	}
}
