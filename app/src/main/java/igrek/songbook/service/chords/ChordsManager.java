package igrek.songbook.service.chords;

import android.graphics.Paint;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.crdfile.CRDModel;
import igrek.songbook.domain.crdfile.CRDParser;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.autoscroll.AutoscrollService;
import igrek.songbook.service.info.UIResourceService;
import igrek.songbook.service.info.UserInfoService;
import igrek.songbook.service.transpose.ChordsTransposer;
import igrek.songbook.view.canvas.CanvasGraphics;
import igrek.songbook.view.canvas.quickmenu.QuickMenu;

public class ChordsManager {
	
	@Inject
	UserInfoService userInfo;
	@Inject
	ChordsTransposer chordsTransposer;
	@Inject
	Lazy<AutoscrollService> autoscrollService;
	@Inject
	UIResourceService uiResourceService;
	@Inject
	CanvasGraphics canvas;
	@Inject
	Lazy<QuickMenu> quickMenu;
	private Logger logger = LoggerFactory.getLogger();
	private int transposed = 0;
	private CRDParser crdParser;
	private CRDModel crdModel;
	private int screenW = 0;
	private Paint paint = null;
	private float fontsize = 26.0f;
	private String originalFileContent = null;
	
	public ChordsManager() {
		DaggerIoc.getFactoryComponent().inject(this);
		crdParser = new CRDParser();
	}
	
	public void reset() {
		transposed = 0;
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
		autoscrollService.get().setFontsize(fontsize);
	}
	
	private void parseAndTranspose(String originalFileContent) {
		String transposedContent = chordsTransposer.transposeContent(originalFileContent, transposed);
		crdModel = crdParser.parseFileContent(transposedContent, screenW, fontsize, paint);
	}
	
	public void transpose(int t) {
		transposed += t;
		if (transposed >= 12)
			transposed -= 12;
		if (transposed <= -12)
			transposed += 12;
		parseAndTranspose(originalFileContent);
	}
	
	public int getTransposed() {
		return transposed;
	}
	
	public String getTransposedString() {
		return (transposed > 0 ? "+" : "") + transposed;
	}
	
	public void onTransposeEvent(int t) {
		transpose(t);
		
		canvas.setCRDModel(getCRDModel());
		
		String info = uiResourceService.resString(R.string.transposition) + ": " + getTransposedString();
		
		if (getTransposed() != 0) { //włączono niezerową transpozycję
			userInfo.showInfoWithAction(info, R.string.transposition_reset, () -> onTransposeResetEvent());
		} else {
			userInfo.showInfo(info);
		}
		
		quickMenu.get().onTransposedEvent();
	}
	
	public void onTransposeResetEvent() {
		onTransposeEvent(-getTransposed());
	}
	
}
