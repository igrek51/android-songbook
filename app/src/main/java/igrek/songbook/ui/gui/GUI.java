package igrek.songbook.ui.gui;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import java.util.List;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.crdfile.CRDModel;
import igrek.songbook.domain.filetree.FileItem;
import igrek.songbook.service.info.UIResourceService;
import igrek.songbook.service.screen.ScreenService;
import igrek.songbook.ui.canvas.CanvasGraphics;
import igrek.songbook.ui.filelist.FileListView;

public class GUI {
	
	protected AppCompatActivity activity;
	protected InputMethodManager imm;
	
	private ActionBar actionBar;
	private FileListView itemsListView;
	private CanvasGraphics canvas = null;
	
	@Inject
	ScreenService screenService;
	
	public GUI(AppCompatActivity activity) {
		this.activity = activity;
		DaggerIoc.getFactoryComponent().inject(this);
		imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	public void hideSoftKeyboard(View window) {
		if (imm != null) {
			imm.hideSoftInputFromWindow(window.getWindowToken(), 0);
		}
	}
	
	public void showSoftKeyboard(View window) {
		if (imm != null) {
			imm.showSoftInput(window, 0);
		}
	}
	
	public void showFileList(String currentDir, List<FileItem> items) {
		
		screenService.setFullscreenLocked(false);
		
		activity.setContentView(R.layout.files_list);
		
		//toolbar
		Toolbar toolbar1 = (Toolbar) activity.findViewById(R.id.toolbar1);
		activity.setSupportActionBar(toolbar1);
		actionBar = activity.getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(true);
		}
		toolbar1.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				TODO event
				//				AppController.sendEvent(new ToolbarBackClickedEvent());
			}
		});
		
//		TODO
//		userInfo.setMainView(activity.findViewById(R.id.mainLayout));
		
		itemsListView = (FileListView) activity.findViewById(R.id.filesList);
		
		itemsListView.init(activity);
		
		updateFileList(currentDir, items);
	}
	
	public void showFileContent() {
		
		screenService.setFullscreenLocked(true);
		
		activity.setContentView(R.layout.file_content);
		
		canvas = new CanvasGraphics(activity);
		
		FrameLayout mainFrame = (FrameLayout) activity.findViewById(R.id.mainFrame);
		
		mainFrame.removeAllViews();
		
		mainFrame.addView(canvas);
		
		
		LayoutInflater inflater = activity.getLayoutInflater();
		View quickMenuView = inflater.inflate(R.layout.quick_menu, null);
		quickMenuView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mainFrame.addView(quickMenuView);
		
		canvas.setQuickMenuView(quickMenuView);
		
//		TODO
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
	
	public void setCRDModel(CRDModel model) {
		//TODO wywalić metodę, odwoływać się bezpośrednio przez Canvas
		canvas.setCRDModel(model);
	}
	
	public void setFontSize(float fontsize) {
		canvas.setFontSizes(fontsize);
	}
	
	public Integer getCurrentScrollPos() {
		return itemsListView.getCurrentScrollPosition();
	}
	
	public void scrollToPosition(int y) {
		itemsListView.scrollToPosition(y);
	}
}
