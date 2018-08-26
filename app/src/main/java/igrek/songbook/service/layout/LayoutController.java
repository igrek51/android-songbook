package igrek.songbook.service.layout;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.service.layout.songpreview.SongPreviewController;
import igrek.songbook.service.layout.songselection.SongSelectionController;

public class LayoutController {
	
	@Inject
	Lazy<SongSelectionController> songSelectionController;
	
	@Inject
	Lazy<SongPreviewController> songPreviewController;
	
	private Logger logger = LoggerFactory.getLogger();
	
	private LayoutState state = LayoutState.SONG_LIST;
	
	public LayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showFileList() {
		songSelectionController.get().showFileList();
	}
	
	public void showFileContent() {
		songPreviewController.get().showFileContent();
	}
	
	public boolean isState(LayoutState compare) {
		return state == compare;
	}
	
	public void setState(LayoutState state) {
		this.state = state;
	}
}
