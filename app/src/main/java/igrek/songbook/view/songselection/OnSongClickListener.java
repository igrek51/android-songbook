package igrek.songbook.view.songselection;

import igrek.songbook.service.songtree.SongTreeItem;

@FunctionalInterface
public interface OnSongClickListener {
	
	void onSongItemClick(SongTreeItem item);
	
}
