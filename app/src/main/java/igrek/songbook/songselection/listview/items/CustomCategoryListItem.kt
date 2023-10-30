package igrek.songbook.songselection.listview.items

import igrek.songbook.persistence.user.custom.CustomCategory

class CustomCategoryListItem constructor(
    val customCategory: CustomCategory,
) : AbstractListItem() {

    override fun simpleName(): String {
        return customCategory.name
    }
}