package igrek.songbook.logic.events;

import igrek.songbook.logic.controller.dispatcher.IEvent;
import igrek.songbook.logic.filetree.FileItem;

public class ItemClickedEvent implements IEvent {

    private int position;
    private FileItem item;

    public ItemClickedEvent(int position, FileItem item) {
        this.position = position;
        this.item = item;
    }

    public int getPosition() {
        return position;
    }

    public FileItem getItem() {
        return item;
    }
}
