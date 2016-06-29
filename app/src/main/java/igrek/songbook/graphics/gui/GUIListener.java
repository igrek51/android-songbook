package igrek.songbook.graphics.gui;

import igrek.songbook.logic.filetree.FileItem;

public interface GUIListener {
    void onToolbarBackClicked();

    void onItemClicked(int position, FileItem item);

    void onResized(int w, int h);
}
