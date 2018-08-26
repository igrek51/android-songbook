package igrek.songbook.service.layout;

public enum LayoutState {
	
	SONG_LIST(1), SONG_PREVIEW(2);
	
	int id;
	
	LayoutState(int id) {
		this.id = id;
	}
}
