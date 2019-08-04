package igrek.songbook.songselection.tree

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.model.Category
import igrek.songbook.songselection.ListScrollPosition

class ScrollPosBuffer {

    private val storedScrollPositions = hashMapOf<Category?, ListScrollPosition>()

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun storeScrollPosition(category: Category?, y: ListScrollPosition?) {
        if (y != null)
            storedScrollPositions[category] = y
    }

    fun restoreScrollPosition(category: Category?): ListScrollPosition? {
        return storedScrollPositions[category]
    }

    fun hasScrollPositionStored(category: Category?): Boolean {
        return storedScrollPositions.containsKey(category)
    }
}
