package igrek.songbook.service.layout.songpreview;

import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.chords.ChordsManager;
import igrek.songbook.service.filetree.FileTreeManager;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.window.WindowManagerService;
import igrek.songbook.view.canvas.CanvasGraphics;

public class SongPreviewController {
	
	@Inject
	FileTreeManager fileTreeManager;
	@Inject
	ChordsManager chordsManager;
	@Inject
	Lazy<LayoutController> layoutController;
	@Inject
	WindowManagerService windowManagerService;
	@Inject
	AppCompatActivity activity;
	
	private Logger logger = LoggerFactory.getLogger();
	
	private CanvasGraphics canvas;
	
	public SongPreviewController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showFileContent() {
		activity.setContentView(R.layout.file_content);
		
		canvas = new CanvasGraphics(activity);
		canvas.reset();
		
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
	
	public void onGraphicsInitializedEvent(int w, int h, Paint paint) {
		//wczytanie pliku i sparsowanie
		String filePath = fileTreeManager.getCurrentFilePath(fileTreeManager.getCurrentFileName());
		String fileContent = fileTreeManager.getFileContent(filePath);
		//inicjalizacja - pierwsze wczytanie pliku
		chordsManager.load(fileContent, w, h, paint);
		
		canvas.setFontSizes(chordsManager.getFontsize());
		canvas.setCRDModel(chordsManager.getCRDModel());
		
		logger.info(chordsManager.getCRDModel().toString());
		
		logger.debug("canvas graphics " + w + "x" + h + " has been initialized");
	}
	
	public void onFontsizeChangedEvent(float fontsize) {
		chordsManager.setFontsize(fontsize);
		//parsowanie bez ponownego wczytywania pliku i wykrywania kodowania
		chordsManager.reparse();
		canvas.setCRDModel(chordsManager.getCRDModel());
	}
	
	public CanvasGraphics getCanvas() {
		return canvas;
	}
}
