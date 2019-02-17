package igrek.songbook.songselection.search;

import igrek.songbook.persistence.songsdb.Song;
import igrek.songbook.songselection.tree.SongTreeItem;

public class SongSearchItem extends SongTreeItem {
	
	private SongSearchItem(Song song) {
		super(song, null);
	}
	
	public static SongSearchItem song(Song song) {
		return new SongSearchItem(song);
	}
	
}
