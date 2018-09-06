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
import igrek.songbook.domain.songsdb.Song;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.chords.LyricsManager;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.layout.LayoutState;
import igrek.songbook.service.layout.MainLayout;
import igrek.songbook.service.system.SoftKeyboardService;
import igrek.songbook.service.system.WindowManagerService;
import igrek.songbook.view.songpreview.CanvasGraphics;
import igrek.songbook.view.songpreview.quickmenu.QuickMenu;

public class SongPreviewLayoutController implements MainLayout {
	
	@Inject
	LyricsManager lyricsManager;
	@Inject
	Lazy<LayoutController> layoutController;
	@Inject
	WindowManagerService windowManagerService;
	@Inject
	AppCompatActivity activity;
	@Inject
	QuickMenu quickMenu;
	@Inject
	AutoscrollService autoscrollService;
	@Inject
	SoftKeyboardService softKeyboardService;
	
	private Logger logger = LoggerFactory.getLogger();
	private CanvasGraphics canvas;
	private Song currentSong;
	
	public SongPreviewLayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	@Override
	public void showLayout(View layout) {
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
		
		softKeyboardService.hideSoftKeyboard();
		
		canvas.setQuickMenuView(quickMenuView);
	}
	
	@Override
	public LayoutState getLayoutState() {
		return LayoutState.SONG_PREVIEW;
	}
	
	@Override
	public int getLayoutResourceId() {
		return R.layout.song_preview;
	}
	
	public void onGraphicsInitializedEvent(int w, int h, Paint paint) {
		// load file and parse it
		String fileContent = currentSong.getFileContent();
		// initialize - first file loading
		lyricsManager.load(fileContent, w, h, paint);
		
		canvas.setFontSizes(lyricsManager.getFontsize());
		canvas.setCRDModel(lyricsManager.getCRDModel());
		
		//logger.debug("canvas graphics " + w + "x" + h + " has been initialized");
	}
	
	public void onFontsizeChangedEvent(float fontsize) {
		lyricsManager.setFontsize(fontsize);
		// parse without reading a whole file again
		lyricsManager.reparse();
		canvas.setCRDModel(lyricsManager.getCRDModel());
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
	
	public void onBackClicked() {
		if (quickMenu.isVisible()) {
			quickMenu.onShowQuickMenuEvent(false);
		} else {
			autoscrollService.onAutoscrollStopEvent();
			
			layoutController.get().showPreviousLayout();
			
			windowManagerService.keepScreenOn(false);
		}
	}
}
