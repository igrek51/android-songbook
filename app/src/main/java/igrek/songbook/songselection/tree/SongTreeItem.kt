package igrek.songbook.songselection.tree

import igrek.songbook.persistence.songsdb.Song
import igrek.songbook.persistence.songsdb.SongCategory

open class SongTreeItem protected constructor(val song: Song?, val category: SongCategory?) {

    val simpleName: String?
        get() = if (isCategory) {
            category?.displayName
        } else {
            song?.title
        }

    val isCategory: Boolean
        get() = !isSong

    val isSong: Boolean
        get() = song != null

    override fun toString(): String {
        return if (isCategory) {
            "[" + category?.displayName + "]"
        } else {
            "" + song?.category?.displayName + " - " + song?.title
        }
    }

    companion object {

        fun song(song: Song): SongTreeItem {
            return SongTreeItem(song, null)
        }

        fun category(category: SongCategory): SongTreeItem {
            return SongTreeItem(null, category)
        }
    }
}
