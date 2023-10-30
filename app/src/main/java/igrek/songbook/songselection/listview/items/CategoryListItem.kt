package igrek.songbook.songselection.listview.items

import igrek.songbook.persistence.general.model.Category

class CategoryListItem constructor(
    val category: Category,
) : AbstractListItem() {

    override fun simpleName(): String {
        return category.displayName.orEmpty()
    }

    override fun id(): String {
        return category.id.toString()
    }
}