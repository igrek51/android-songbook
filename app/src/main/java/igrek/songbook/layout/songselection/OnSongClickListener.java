package igrek.songbook.layout.songselection;

import igrek.songbook.layout.songtree.SongTreeItem;

@FunctionalInterface
public interface OnSongClickListener {
	
	void onSongItemClick(SongTreeItem item);
	
}
