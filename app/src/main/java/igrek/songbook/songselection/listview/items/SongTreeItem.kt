package igrek.songbook.songselection.listview.items

import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.user.custom.CustomCategory

open class SongTreeItem protected constructor(
    open val song: Song? = null,
    val category: Category? = null,
    val customCategory: CustomCategory? = null,
) {

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
            return SongTreeItem(song=song)
        }

        fun category(category: Category): SongTreeItem {
            return SongTreeItem(category=category)
        }

        fun customCategory(customCategory: CustomCategory): SongTreeItem {
            return SongTreeItem(customCategory=customCategory)
        }
    }
}
