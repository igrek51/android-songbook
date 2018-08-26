package igrek.songbook.service.layout.songpreview;

import android.graphics.Paint;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.chords.ChordsManager;
import igrek.songbook.service.filetree.FileTreeManager;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.view.canvas.CanvasGraphics;

public class SongPreviewController {
	
	@Inject
	FileTreeManager fileTreeManager;
	@Inject
	ChordsManager chordsManager;
	@Inject
	Lazy<LayoutController> layoutController;
	@Inject
	CanvasGraphics canvas;
	
	private Logger logger = LoggerFactory.getLogger();
	
	public SongPreviewController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void onGraphicsInitializedEvent(int w, int h, Paint paint) {
		//wczytanie pliku i sparsowanie
		String filePath = fileTreeManager.getCurrentFilePath(fileTreeManager.getCurrentFileName());
		String fileContent = fileTreeManager.getFileContent(filePath);
		//inicjalizacja - pierwsze wczytanie pliku
		chordsManager.load(fileContent, w, h, paint);
		
		canvas.setFontSize(chordsManager.getFontsize());
		canvas.setCRDModel(chordsManager.getCRDModel());
		
		logger.debug("canvas graphics has been initialized");
	}
	
	public void onFontsizeChangedEvent(float fontsize) {
		chordsManager.setFontsize(fontsize);
		//parsowanie bez ponownego wczytywania pliku i wykrywania kodowania
		chordsManager.reparse();
		canvas.setCRDModel(chordsManager.getCRDModel());
	}
	
}
