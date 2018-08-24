package igrek.songbook.ui.gui;

import java.util.HashMap;

import igrek.songbook.service.controller.services.IService;

public class ScrollPosBuffer implements IService {

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
