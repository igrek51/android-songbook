package igrek.songbook.songselection.tree

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.songsdb.SongCategory
import igrek.songbook.songselection.ListScrollPosition

class ScrollPosBuffer {

    private val storedScrollPositions = hashMapOf<SongCategory?, ListScrollPosition>()

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun storeScrollPosition(category: SongCategory?, y: ListScrollPosition?) {
        if (y != null)
            storedScrollPositions[category] = y
    }

    fun restoreScrollPosition(category: SongCategory?): ListScrollPosition? {
        return storedScrollPositions[category]
    }

    fun hasScrollPositionStored(category: SongCategory?): Boolean {
        return storedScrollPositions.containsKey(category)
    }
}
