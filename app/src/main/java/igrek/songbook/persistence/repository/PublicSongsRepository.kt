package igrek.songbook.persistence.repository

import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.util.lookup.LazyFinderByTuple
import igrek.songbook.util.lookup.SimpleCache

data class PublicSongsRepository(
    val versionNumber: Long,
    val categories: SimpleCache<List<Category>>,
    val songs: SimpleCache<List<Song>>
) {
    val categoryFinder = LazyFinderByTuple(
        entityToId = { e -> e.id },
        valuesSupplier = categories
    )

    val songFinder = LazyFinderByTuple(
        entityToId = { song -> song.songIdentifier() },
        valuesSupplier = songs
    )

    fun invalidate() {
        categoryFinder.invalidate()
        songFinder.invalidate()
    }

}