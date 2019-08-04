package igrek.songbook.songselection.tree

import com.google.common.base.Function
import com.google.common.collect.Ordering
import igrek.songbook.persistence.model.CategoryType
import java.text.Collator
import java.util.*

class SongTreeSorter {

    private val locale = Locale("pl", "PL")
    private val stringCollator = Collator.getInstance(locale)

    private val categoryTypeExtractor: (SongTreeItem?) -> CategoryType? = { item ->
        item?.category?.type
    }

    private val categorySongOrdering = Ordering.natural<Boolean>()
            .onResultOf(Function<SongTreeItem, Boolean> { it?.isCategory })

    private val categoryTypeOrdering = //
            Ordering.explicit(CategoryType.CUSTOM, CategoryType.ARTIST, CategoryType.OTHERS)
                    .nullsLast<CategoryType>()
                    .onResultOf<SongTreeItem>(categoryTypeExtractor)

    private val itemNameOrdering = Ordering.from<SongTreeItem> { lhs, rhs ->
        val lName = lhs.simpleName!!.toLowerCase(locale)
        val rName = rhs.simpleName!!.toLowerCase(locale)
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
