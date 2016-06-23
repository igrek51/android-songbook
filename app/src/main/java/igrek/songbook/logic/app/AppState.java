package igrek.todotree.logic.app;

public enum AppState {
    ITEMS_LIST(1),
    EDIT_ITEM_CONTENT(2);

    int id;

    AppState(int id){
        this.id = id;
    }
}
