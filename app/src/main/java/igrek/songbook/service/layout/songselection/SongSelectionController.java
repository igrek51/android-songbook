package igrek.songbook.service.layout.songselection;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.exception.NoParentDirException;
import igrek.songbook.service.activity.ActivityController;
import igrek.songbook.service.filetree.FileItem;
import igrek.songbook.service.filetree.FileTreeManager;
import igrek.songbook.service.filetree.ScrollPosBuffer;
import igrek.songbook.service.info.UIResourceService;
import igrek.songbook.service.info.UserInfoService;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.layout.LayoutState;
import igrek.songbook.service.preferences.PreferencesDefinition;
import igrek.songbook.service.preferences.PreferencesService;
import igrek.songbook.service.window.WindowManagerService;

public class SongSelectionController {
	
	@Inject
	FileTreeManager fileTreeManager;
	@Inject
	Lazy<ActivityController> activityController;
	@Inject
	LayoutController layoutController;
	@Inject
	WindowManagerService windowManagerService;
	@Inject
	PreferencesService preferencesService;
	@Inject
	UserInfoService userInfoService;
	@Inject
	ScrollPosBuffer scrollPosBuffer;
	@Inject
	UIResourceService uiResourceService;
	
	public SongSelectionController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void goUp() {
		try {
			fileTreeManager.goUp();
			updateFileList();
			//scrollowanie do ostatnio otwartego folderu
			restoreScrollPosition(fileTreeManager.getCurrentPath());
		} catch (NoParentDirException e) {
			activityController.get().quit();
		}
	}
	
	private void updateFileList() {
		layoutController.updateFileList(fileTreeManager.getCurrentDirName(), fileTreeManager.getItems());
		layoutController.setState(LayoutState.SONG_LIST);
	}
	
	private void showFileContent(String filename) {
		layoutController.setState(LayoutState.SONG_PREVIEW);
		fileTreeManager.setCurrentFileName(filename);
		layoutController.showFileContent();
		windowManagerService.keepScreenOn();
	}
	
	private String getHomePath() {
		return preferencesService.getValue(PreferencesDefinition.startPath, String.class);
	}
	
	private boolean isInHomeDir() {
		return fileTreeManager.getCurrentPath().equals(FileTreeManager.trimEndSlash(getHomePath()));
	}
	
	public void homeClicked() {
		if (isInHomeDir()) {
			activityController.get().quit();
		} else {
			String homePath = getHomePath();
			if (homePath == null) {
				userInfoService.showInfo(R.string.message_home_not_set);
			}else {
				fileTreeManager.goTo(homePath);
				updateFileList();
			}
		}
	}
	
	public void setHomePath() {
		String homeDir = fileTreeManager.getCurrentPath();
		preferencesService.setValue(PreferencesDefinition.startPath, homeDir);
		preferencesService.saveAll();
		userInfoService.showInfo(R.string.starting_directory_saved, R.string.action_info_ok);
	}
	
	
	public void restoreScrollPosition(String path) {
		Integer savedScrollPos = scrollPosBuffer.restoreScrollPosition(path);
		if (savedScrollPos != null) {
			layoutController.scrollToPosition(savedScrollPos);
		}
	}
	
	public void showUIHelp() {
		String message = uiResourceService.resString(R.string.ui_help_content);
		String title = uiResourceService.resString(R.string.ui_help);
		userInfoService.showDialog(title, message);
	}
	
	public void onToolbarBackClickedEvent() {
		goUp();
	}
	
	public void onItemClickedEvent(int posistion, FileItem item) {
		scrollPosBuffer.storeScrollPosition(fileTreeManager.getCurrentPath(), layoutController.getCurrentScrollPos());
		if (item.isDirectory()) {
			fileTreeManager.goInto(item.getName());
			updateFileList();
			layoutController.scrollToItem(0);
		} else {
			showFileContent(item.getName());
		}
	}
	
	
}
