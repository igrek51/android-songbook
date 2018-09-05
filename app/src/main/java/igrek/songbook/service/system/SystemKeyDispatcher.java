package igrek.songbook.service.system;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.service.layout.LayoutController;
import igrek.songbook.service.layout.LayoutState;
import igrek.songbook.service.layout.search.SongSearchLayoutController;
import igrek.songbook.service.layout.songpreview.SongPreviewLayoutController;
import igrek.songbook.service.layout.songtree.SongTreeLayoutController;

public class SystemKeyDispatcher {
	
	@Inject
	LayoutController layoutController;
	@Inject
	SongTreeLayoutController songTreeLayoutController;
	@Inject
	SongSearchLayoutController songSearchLayoutController;
	@Inject
	SongPreviewLayoutController songPreviewLayoutController;
	
	public SystemKeyDispatcher() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public boolean onKeyBack() {
		if (layoutController.isState(LayoutState.SONGS_TREE)) {
			songTreeLayoutController.onBackClicked();
		} else if (layoutController.isState(LayoutState.SEARCH_SONG)) {
			songSearchLayoutController.onBackClicked();
		} else if (layoutController.isState(LayoutState.SONG_PREVIEW)) {
			songPreviewLayoutController.onBackClicked();
		}
		return true;
	}
	
	public boolean onKeyMenu() {
		return false;
	}
}
