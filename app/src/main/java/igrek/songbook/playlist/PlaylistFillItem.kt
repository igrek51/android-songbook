package igrek.songbook.playlist

import igrek.songbook.persistence.general.model.Song
import igrek.songbook.songselection.listview.items.SongTreeItem

class PlaylistFillItem private constructor(song: Song) : SongTreeItem(song, null) {

    companion object {
        fun song(song: Song): PlaylistFillItem {
            return PlaylistFillItem(song)
        }
    }

}
