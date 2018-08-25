package igrek.songbook.service.layout;

public enum LayoutState {
	
	FILE_LIST(1), FILE_CONTENT(2);
	
	int id;
	
	LayoutState(int id) {
		this.id = id;
	}
}
