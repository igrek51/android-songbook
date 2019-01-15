package igrek.songbook.songselection;

import igrek.songbook.persistence.songsdb.Song;
import igrek.songbook.persistence.songsdb.SongCategory;

public class SongSearchItem extends SongTreeItem {
	
	protected SongSearchItem(Song song, SongCategory category) {
		super(song, category);
	}
	
	public static SongSearchItem song(Song song) {
		return new SongSearchItem(song, null);
	}
	
}
