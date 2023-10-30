package igrek.songbook.songselection.listview.items

import igrek.songbook.persistence.general.model.Song

open class SongListItem constructor(
    val song: Song,
) : AbstractListItem() {

    override fun simpleName(): String {
        return song.title
    }

    override fun id(): String {
        return song.id
    }
}