package igrek.songbook.songselection

import igrek.songbook.songselection.listview.items.SongTreeItem

interface SongClickListener {

    fun onSongItemClick(item: SongTreeItem)

    fun onSongItemLongClick(item: SongTreeItem)

}
