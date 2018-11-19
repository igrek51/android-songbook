package igrek.songbook.layout.songselection;

import igrek.songbook.model.songsdb.Song;
import igrek.songbook.model.songsdb.SongCategory;

public class SongSearchItem extends SongTreeItem {
	
	protected SongSearchItem(Song song, SongCategory category) {
		super(song, category);
	}
	
	public static SongSearchItem song(Song song) {
		return new SongSearchItem(song, null);
	}
	
}
