package igrek.songbook.view.songlist;

import igrek.songbook.service.songtree.SongTreeItem;

@FunctionalInterface
public interface OnSongClickListener {
	
	void onSongItemClick(SongTreeItem item);
	
}
