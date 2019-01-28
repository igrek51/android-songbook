package igrek.songbook.songselection;

import igrek.songbook.persistence.songsdb.Song;

public class SongSearchItem extends SongTreeItem {
	
	private SongSearchItem(Song song) {
		super(song, null);
	}
	
	public static SongSearchItem song(Song song) {
		return new SongSearchItem(song);
	}
	
}
