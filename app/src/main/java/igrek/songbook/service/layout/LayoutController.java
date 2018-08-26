package igrek.songbook.service.layout;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.filetree.FileItem;
import igrek.songbook.service.filetree.FileTreeManager;
import igrek.songbook.service.layout.songselection.SongSelectionController;
import igrek.songbook.service.window.WindowManagerService;
import igrek.songbook.view.canvas.CanvasGraphics;
import igrek.songbook.view.filelist.FileListView;

public class LayoutController {
	
	@Inject
	AppCompatActivity activity;
	
	@Inject
	WindowManagerService windowManagerService;
	
	@Inject
	FileTreeManager fileTreeManager;
	
	@Inject
	Lazy<SongSelectionController> songSelectionController;
	
	private Logger logger = LoggerFactory.getLogger();
	
	private LayoutState state = LayoutState.SONG_LIST;
	private ActionBar actionBar;
	private FileListView itemsListView;
	private CanvasGraphics canvas = null;
	
	public LayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showFileList() {
		
		String currentDir = fileTreeManager.getCurrentDirName();
		List<FileItem> items = fileTreeManager.getItems();
		
		windowManagerService.setFullscreenLocked(false);
		
		logger.info("activity: " + activity);
		
		activity.setContentView(R.layout.files_list);
		
		//toolbar
		Toolbar toolbar1 = activity.findViewById(R.id.toolbar1);
		activity.setSupportActionBar(toolbar1);
		actionBar = activity.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
		}
		toolbar1.setNavigationOnClickListener(v -> {
			songSelectionController.get().onToolbarBackClickedEvent();
		});
		
		//		userInfo.setMainView(activity.findViewById(R.id.mainLayout));
		
		itemsListView = activity.findViewById(R.id.filesList);
		
		itemsListView.init(activity);
		
		updateFileList(currentDir, items);
	}
	
	public void showFileContent() {
		
		windowManagerService.setFullscreenLocked(true);
		
		activity.setContentView(R.layout.file_content);
		
		canvas = new CanvasGraphics(activity);
		
		FrameLayout mainFrame = activity.findViewById(R.id.mainFrame);
		
		mainFrame.removeAllViews();
		
		mainFrame.addView(canvas);
		
		
		LayoutInflater inflater = activity.getLayoutInflater();
		View quickMenuView = inflater.inflate(R.layout.quick_menu, null);
		quickMenuView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mainFrame.addView(quickMenuView);
		
		canvas.setQuickMenuView(quickMenuView);
		
		//		userInfo.setMainView(mainFrame);
	}
	
	public void updateFileList(String currentDir, List<FileItem> items) {
		setTitle(currentDir);
		//lista elementów
		itemsListView.setItems(items);
	}
	
	public void scrollToItem(int position) {
		itemsListView.scrollTo(position);
	}
	
	public void setTitle(String title) {
		actionBar.setTitle(title);
	}
	
	public Integer getCurrentScrollPos() {
		return itemsListView.getCurrentScrollPosition();
	}
	
	public void scrollToPosition(int y) {
		itemsListView.scrollToPosition(y);
	}
	
	public boolean isState(LayoutState compare) {
		return state == compare;
	}
	
	public void setState(LayoutState state) {
		this.state = state;
	}
}
