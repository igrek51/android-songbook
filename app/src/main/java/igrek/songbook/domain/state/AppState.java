package igrek.songbook.domain.state;

public enum AppState {
	
	FILE_LIST(1),
	FILE_CONTENT(2);

	int id;

	AppState(int id){
		this.id = id;
	}
}
