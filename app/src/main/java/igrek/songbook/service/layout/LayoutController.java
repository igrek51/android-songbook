package igrek.songbook.service.layout;

import javax.inject.Inject;

import dagger.Lazy;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.service.layout.songpreview.SongPreviewController;
import igrek.songbook.service.layout.songselection.SongSelectionController;

public class LayoutController {
	
	@Inject
	Lazy<SongSelectionController> songSelectionController;
	
	@Inject
	Lazy<SongPreviewController> songPreviewController;
	
	private LayoutState state = LayoutState.SONG_LIST;
	
	public LayoutController() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void showSongSelection() {
		songSelectionController.get().showSongSelection();
	}
	
	public void showSongPreview() {
		songPreviewController.get().showSongPreview();
	}
	
	public boolean isState(LayoutState compare) {
		return state == compare;
	}
	
	public void setState(LayoutState state) {
		this.state = state;
	}
}
