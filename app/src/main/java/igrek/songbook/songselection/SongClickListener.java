package igrek.songbook.songselection;

import igrek.songbook.songselection.tree.SongTreeItem;

public interface SongClickListener {
	
	void onSongItemClick(SongTreeItem item);
	
	void onSongItemLongClick(SongTreeItem item);
	
}
