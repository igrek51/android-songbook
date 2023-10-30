package igrek.songbook.songselection.tree

import com.google.common.base.Function
import com.google.common.collect.Ordering
import igrek.songbook.persistence.general.model.CategoryType
import igrek.songbook.songselection.listview.items.SongTreeItem
import java.text.Collator
import java.util.Collections
import java.util.Locale

class SongTreeSorter {

    private val locale = Locale("pl", "PL")
    private val stringCollator = Collator.getInstance(locale)

    private val categoryTypeExtractor: (SongTreeItem?) -> CategoryType? = { item ->
        item?.category?.type
    }

    private val categorySongOrdering = Ordering.natural<Boolean>()
        .onResultOf(Function<SongTreeItem, Boolean> { it?.isCategory == true })

    private val categoryTypeOrdering = //
        Ordering.explicit(CategoryType.CUSTOM, CategoryType.ARTIST, CategoryType.OTHERS)
            .nullsLast<CategoryType>()
            .onResultOf<SongTreeItem>(categoryTypeExtractor)

    private val itemNameOrdering = Ordering.from<SongTreeItem> { lhs, rhs ->
        val lName = lhs.simpleName!!.lowercase(locale)
        val rName = rhs.simpleName!!.lowercase(locale)
        stringCollator.compare(lName, rName)
    }

    private val songTreeItemOrdering = categorySongOrdering // songs before categories
        .compound(categoryTypeOrdering) // CUSTOM, ARTIST (...), OTHERS
        .compound(itemNameOrdering) // sort by name

    fun sort(items: List<SongTreeItem>): List<SongTreeItem> {
        // make it modifiable
        val modifiableList = ArrayList(items)
        Collections.sort(modifiableList, songTreeItemOrdering)
        return modifiableList
    }
}
