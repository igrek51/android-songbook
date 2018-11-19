package igrek.songbook.layout.songselection;

@FunctionalInterface
public interface OnSongClickListener {
	
	void onSongItemClick(SongTreeItem item);
	
}
