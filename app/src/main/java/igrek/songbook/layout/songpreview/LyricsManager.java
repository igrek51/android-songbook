package igrek.songbook.layout.songpreview;

import android.graphics.Paint;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.chords.ChordsNotation;
import igrek.songbook.domain.lyrics.LyricsModel;
import igrek.songbook.domain.lyrics.LyricsParser;
import igrek.songbook.layout.songpreview.autoscroll.AutoscrollService;
import igrek.songbook.layout.songpreview.transpose.ChordsTransposerManager;
import igrek.songbook.persistence.preferences.PreferencesDefinition;
import igrek.songbook.persistence.preferences.PreferencesService;
import igrek.songbook.system.WindowManagerService;

public class LyricsManager {
	
	@Inject
	Lazy<ChordsTransposerManager> chordsTransposerManager;
	@Inject
	Lazy<AutoscrollService> autoscrollService;
	@Inject
	PreferencesService preferencesService;
	@Inject
	WindowManagerService windowManagerService;
	
	private LyricsParser lyricsParser;
	private LyricsModel lyricsModel;
	private int screenW = 0;
	private Paint paint;
	private float fontsize;
	private ChordsNotation chordsNotation;
	private String originalFileContent;
	
	public LyricsManager() {
		DaggerIoc.getFactoryComponent().inject(this);
		lyricsParser = new LyricsParser();
		loadPreferences();
	}
	
	private void loadPreferences() {
		fontsize = preferencesService.getValue(PreferencesDefinition.fontsize, Float.class);
		long chordsNotationId = preferencesService.getValue(PreferencesDefinition.chordsNotationId, Long.class);
		chordsNotation = ChordsNotation.Companion.parseById(chordsNotationId);
	}
	
	private void reset() {
		chordsTransposerManager.get().reset();
		autoscrollService.get().reset();
	}
	
	public void load(String fileContent, Integer screenW, Integer screenH, Paint paint) {
		reset();
		originalFileContent = fileContent;
		
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
	
	public float getFontsize() {
		return fontsize;
	}
	
	public void setFontsize(float fontsize) {
		if (fontsize < 1)
			fontsize = 1;
		this.fontsize = fontsize;
	}
	
	public void setChordsNotation(ChordsNotation chordsNotation) {
		this.chordsNotation = chordsNotation;
	}
	
	public ChordsNotation getChordsNotation() {
		return chordsNotation;
	}
	
	private void parseAndTranspose(String originalFileContent) {
		String transposedContent = chordsTransposerManager.get()
				.transposeContent(originalFileContent);
		float realFontsize = windowManagerService.dp2px(this.fontsize);
		lyricsModel = lyricsParser.parseFileContent(transposedContent, screenW, realFontsize, paint);
	}
	
}
