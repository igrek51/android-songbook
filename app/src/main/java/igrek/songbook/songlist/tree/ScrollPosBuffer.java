package igrek.songbook.songlist.tree;

import java.util.HashMap;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.songsdb.SongCategory;
import igrek.songbook.songlist.view.ListScrollPosition;

public class ScrollPosBuffer {
	
	private HashMap<SongCategory, ListScrollPosition> storedScrollPositions = new HashMap<>();
	
	public ScrollPosBuffer() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public void storeScrollPosition(SongCategory category, ListScrollPosition y) {
		if (y != null)
			storedScrollPositions.put(category, y);
	}
	
	public ListScrollPosition restoreScrollPosition(SongCategory category) {
		return storedScrollPositions.get(category);
	}
	
	public boolean hasScrollPositionStored(SongCategory category) {
		return storedScrollPositions.containsKey(category);
	}
}
