package igrek.songbook.songlist.view;

import igrek.songbook.songlist.tree.SongTreeItem;

@FunctionalInterface
public interface OnSongClickListener {
	
	void onSongItemClick(SongTreeItem item);
	
}
