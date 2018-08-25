package igrek.songbook.service.layout.songpreview;

import igrek.songbook.dagger.DaggerIoc;

public class SongPreviewController {
	
	public SongPreviewController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	//	@Override
	//	public void onEvent(IEvent event) {
	//		if (event instanceof GraphicsInitializedEvent) {
	//
	//			int w = ((GraphicsInitializedEvent) event).getW();
	//			int h = ((GraphicsInitializedEvent) event).getH();
	//			Paint paint = ((GraphicsInitializedEvent) event).getPaint();
	//
	//			//wczytanie pliku i sparsowanie
	//			String filePath = fileTreeManager.getCurrentFilePath(fileTreeManager.getCurrentFileName());
	//			String fileContent = fileTreeManager.getFileContent(filePath);
	//			//inicjalizacja - pierwsze wczytanie pliku
	//			chordsManager.load(fileContent, w, h, paint);
	//
	//			gui.setFontSize(chordsManager.getFontsize());
	//			gui.setCRDModel(chordsManager.getCRDModel());
	//
	//		} else if (event instanceof FontsizeChangedEvent) {
	//
	//			float fontsize = ((FontsizeChangedEvent) event).getFontsize();
	//
	//			chordsManager.setFontsize(fontsize);
	//			//parsowanie bez ponownego wczytywania pliku i wykrywania kodowania
	//			chordsManager.reparse();
	//			gui.setCRDModel(chordsManager.getCRDModel());
	//
	//		}
	//	}
	
}
