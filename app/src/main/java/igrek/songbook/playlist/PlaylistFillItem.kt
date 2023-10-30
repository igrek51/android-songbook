package igrek.songbook.playlist

import igrek.songbook.persistence.general.model.Song
import igrek.songbook.songselection.listview.items.SongListItem

class PlaylistFillItem constructor(
    song: Song,
) : SongListItem(song)
