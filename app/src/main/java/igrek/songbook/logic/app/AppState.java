package igrek.songbook.logic.app;

public enum AppState {
    FILE_LIST(1),
    FILE_CONTENT(2);

    int id;

    AppState(int id){
        this.id = id;
    }
}
