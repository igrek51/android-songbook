package igrek.songbook.songselection

import igrek.songbook.songselection.tree.SongTreeItem

interface SongClickListener {

    fun onSongItemClick(item: SongTreeItem)

    fun onSongItemLongClick(item: SongTreeItem)

}
