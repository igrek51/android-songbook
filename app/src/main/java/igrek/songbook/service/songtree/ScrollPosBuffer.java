package igrek.songbook.service.songtree;

import java.util.HashMap;

import igrek.songbook.dagger.DaggerIoc;

public class ScrollPosBuffer {
	
	private HashMap<String, Integer> storedScrollPositions = new HashMap<>();
	
	public ScrollPosBuffer() {
		DaggerIoc.getFactoryComponent().inject(this);
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
