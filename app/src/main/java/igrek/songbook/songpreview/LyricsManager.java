package igrek.songbook.songpreview;

import android.graphics.Paint;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.model.lyrics.LyricsModel;
import igrek.songbook.model.lyrics.LyricsParser;
import igrek.songbook.songpreview.autoscroll.AutoscrollService;
import igrek.songbook.songpreview.theme.LyricsThemeService;
import igrek.songbook.songpreview.transpose.ChordsTransposerManager;
import igrek.songbook.system.WindowManagerService;

public class LyricsManager {
	
	@Inject
	Lazy<ChordsTransposerManager> chordsTransposerManager;
	@Inject
	Lazy<AutoscrollService> autoscrollService;
	@Inject
	LyricsThemeService lyricsThemeService;
	@Inject
	WindowManagerService windowManagerService;
	
	private LyricsParser lyricsParser;
	private LyricsModel lyricsModel;
	private int screenW = 0;
	private Paint paint;
	private String originalFileContent;
	
	public LyricsManager() {
		DaggerIoc.getFactoryComponent().inject(this);
		lyricsParser = new LyricsParser();
	}
	
	private void reset() {
		chordsTransposerManager.get().reset();
		autoscrollService.get().reset();
	}
	
	private String normalizeContent(String content) {
		content = content.replace("\r", "");
		content = content.replace("\t", " ");
		content = content.replace("\u00A0", " "); // NO-BREAK SPACE (0xC2 0xA0)
		return content;
	}
	
	public void load(String fileContent, Integer screenW, Integer screenH, Paint paint) {
		reset();
		originalFileContent = normalizeContent(fileContent);
		
		if (screenW != null)
			this.screenW = screenW;
		if (paint != null)
			this.paint = paint;
		
		lyricsParser = new LyricsParser();
		
		parseAndTranspose(originalFileContent);
	}
	
	public void onPreviewSizeChange(int screenW, int screenH, Paint paint) {
		this.screenW = screenW;
		this.paint = paint;
		reparse();
	}
	
	public void reparse() {
		parseAndTranspose(originalFileContent);
	}
	
	public LyricsModel getCRDModel() {
		return lyricsModel;
	}
	
	private void parseAndTranspose(String originalFileContent) {
		String transposedContent = chordsTransposerManager.get()
				.transposeContent(originalFileContent);
		float realFontsize = windowManagerService.dp2px(lyricsThemeService.getFontsize());
		lyricsModel = lyricsParser.parseFileContent(transposedContent, screenW, realFontsize, paint);
	}
	
}
