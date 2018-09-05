package igrek.songbook.service.songtree;

import java.util.HashMap;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.songsdb.SongCategory;

public class ScrollPosBuffer {
	
	private HashMap<SongCategory, Integer> storedScrollPositions = new HashMap<>();
	
	public ScrollPosBuffer() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void storeScrollPosition(SongCategory category, Integer y) {
		if (y != null)
			storedScrollPositions.put(category, y);
	}
	
	public Integer restoreScrollPosition(SongCategory category) {
		return storedScrollPositions.get(category);
	}
}
