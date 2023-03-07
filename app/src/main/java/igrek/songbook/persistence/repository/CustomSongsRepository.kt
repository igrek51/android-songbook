package igrek.songbook.persistence.repository

import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.util.lookup.LazyFinderByTuple
import igrek.songbook.util.lookup.SimpleCache

data class CustomSongsRepository(
    val songs: SimpleCache<List<Song>>,
    val uncategorizedSongs: SimpleCache<List<Song>>,
    val allCustomCategory: Category,
) {

    val songFinder = LazyFinderByTuple(
        entityToId = { song -> song.songIdentifier() },
        valuesSupplier = songs
    )

    fun invalidate() {
        songs.invalidate()
        uncategorizedSongs.invalidate()

        songFinder.invalidate()
    }
}