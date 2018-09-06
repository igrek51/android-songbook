package igrek.songbook.service.chords;

import android.graphics.Paint;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.crd.CRDModel;
import igrek.songbook.domain.crd.CRDParser;
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.chords.transpose.ChordsTransposerManager;
import igrek.songbook.service.preferences.PreferencesDefinition;
import igrek.songbook.service.preferences.PreferencesService;

public class LyricsManager {
	
	@Inject
	Lazy<ChordsTransposerManager> chordsTransposerManager;
	@Inject
	Lazy<AutoscrollService> autoscrollService;
	@Inject
	PreferencesService preferencesService;
	
	private CRDParser crdParser;
	private CRDModel crdModel;
	private int screenW = 0;
	private Paint paint;
	private float fontsize;
	private String originalFileContent;
	
	public LyricsManager() {
		DaggerIoc.getFactoryComponent().inject(this);
		crdParser = new CRDParser();
		loadPreferences();
	}
	
	private void loadPreferences() {
		fontsize = preferencesService.getValue(PreferencesDefinition.fontsize, Float.class);
	}
	
	private void reset() {
		chordsTransposerManager.get().reset();
		autoscrollService.get().reset();
	}
	
	public void load(String fileContent, Integer screenW, Integer screenH, Paint paint) {
		reset();
		originalFileContent = fileContent;
		
		if (screenW != null) {
			this.screenW = screenW;
		}
		if (paint != null) {
			this.paint = paint;
		}
		
		crdParser = new CRDParser();
		
		parseAndTranspose(originalFileContent);
	}
	
	
	public void reparse() {
		parseAndTranspose(originalFileContent);
	}
	
	public CRDModel getCRDModel() {
		return crdModel;
	}
	
	public float getFontsize() {
		return fontsize;
	}
	
	public void setFontsize(float fontsize) {
		if (fontsize < 1)
			fontsize = 1;
		this.fontsize = fontsize;
	}
	
	private void parseAndTranspose(String originalFileContent) {
		String transposedContent = chordsTransposerManager.get()
				.transposeContent(originalFileContent);
		crdModel = crdParser.parseFileContent(transposedContent, screenW, fontsize, paint);
	}
	
}
