package igrek.todotree.gui;

import java.util.List;

import igrek.todotree.logic.datatree.TreeItem;

public interface GUIListener {
    void onToolbarBackClicked();

    void onAddItemClicked();

    void onAddItemClicked(int position);

    void onItemClicked(int position, TreeItem item);

    void onItemEditClicked(int position, TreeItem item);

    void onItemGoIntoClicked(int position, TreeItem item);

    void onItemRemoveClicked(int position, TreeItem item);

    void onSavedEditedItem(TreeItem editedItem, String content);

    void onCancelEditedItem(TreeItem editedItem);

    void onSavedNewItem(String content);

    void onSaveAndAddItem(TreeItem editedItem, String content);

    /**
     * @param position pozycja elementu przed przesuwaniem
     * @param step liczba pozycji do przesunięcia (dodatnia - w dół, ujemna - w górę)
     * @return nowa lista elementów
     */
    List<TreeItem> onItemMoved(int position, int step);

    void onItemLongClick(int position);

    void onSelectedClicked(int position, TreeItem item, boolean checked);
}
