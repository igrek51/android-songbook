package igrek.songbook.songselection.tree

import igrek.songbook.persistence.model.Category
import igrek.songbook.persistence.model.Song

open class SongTreeItem protected constructor(open val song: Song?, val category: Category?) {

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
            "" + song?.categories?.joinToString(", ") { c -> c.displayName!! } + " - " + song?.title
        }
    }

    companion object {

        fun song(song: Song): SongTreeItem {
            return SongTreeItem(song, null)
        }

        fun category(category: Category): SongTreeItem {
            return SongTreeItem(null, category)
        }
    }
}
