package igrek.songbook.graphics.gui;

import java.util.HashMap;

public class ScrollPosBuffer {

    private HashMap<String, Integer> storedScrollPositions;

    public ScrollPosBuffer() {
        storedScrollPositions = new HashMap<>();
    }

    public void storeScrollPosition(String path, Integer y) {
        if (path != null && y != null) {
            storedScrollPositions.put(path, y);
        }
    }

    public Integer restoreScrollPosition(String path) {
        return storedScrollPositions.get(path);
    }
}
