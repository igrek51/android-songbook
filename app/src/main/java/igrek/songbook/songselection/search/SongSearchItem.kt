package igrek.songbook.songselection.search

import igrek.songbook.persistence.general.model.Song
import igrek.songbook.songselection.listview.items.SongTreeItem

class SongSearchItem private constructor(song: Song) : SongTreeItem(song, null) {

    companion object {
        fun song(song: Song): SongSearchItem {
            return SongSearchItem(song)
        }
    }

}
