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
import igrek.songbook.domain.song.Song;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.chords.ChordsManager;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.songtree.SongTreeWalker;
import igrek.songbook.service.window.WindowManagerService;
import igrek.songbook.view.songpreview.CanvasGraphics;

public class SongPreviewLayoutController {
	
	@Inject
	SongTreeWalker songTreeWalker;
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
	private Song currentSong;
	
	public SongPreviewLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showSongPreview(View layout) {
		canvas = new CanvasGraphics(activity);
		canvas.reset();
		
		windowManagerService.keepScreenOn(true);
		
		FrameLayout mainFrame = layout.findViewById(R.id.mainFrame);
		mainFrame.removeAllViews();
		mainFrame.addView(canvas);
		
		LayoutInflater inflater = activity.getLayoutInflater();
		View quickMenuView = inflater.inflate(R.layout.quick_menu, null);
		quickMenuView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		mainFrame.addView(quickMenuView);
		
		canvas.setQuickMenuView(quickMenuView);
	}
	
	public void onGraphicsInitializedEvent(int w, int h, Paint paint) {
		// load file and parse it
		String fileContent = currentSong.getFileContent();
		// initialize - first file loading
		chordsManager.load(fileContent, w, h, paint);
		
		canvas.setFontSizes(chordsManager.getFontsize());
		canvas.setCRDModel(chordsManager.getCRDModel());
		
		logger.debug("canvas graphics " + w + "x" + h + " has been initialized");
	}
	
	public void onFontsizeChangedEvent(float fontsize) {
		chordsManager.setFontsize(fontsize);
		// parse without reading a whole file again
		chordsManager.reparse();
		canvas.setCRDModel(chordsManager.getCRDModel());
	}
	
	public CanvasGraphics getCanvas() {
		return canvas;
	}
	
	public Song getCurrentSong() {
		return currentSong;
	}
	
	public void setCurrentSong(Song currentSong) {
		this.currentSong = currentSong;
	}
}
