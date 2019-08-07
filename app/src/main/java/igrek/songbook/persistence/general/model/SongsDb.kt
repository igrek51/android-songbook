package igrek.songbook.persistence.general.model

import igrek.songbook.util.lookup.FinderById
import igrek.songbook.util.lookup.FinderByTuple
import igrek.songbook.util.lookup.SimpleCache

data class SongsDb(
        val versionNumber: Long,
        val categories: List<Category>,
        val songs: List<Song>
) {
    val songFinder = FinderByTuple(songs) { song -> song.songIdentifier() }
    val categoryFinder = FinderById(categories) { e -> e.id }

    private var customSongsCache: SimpleCache<List<Song>> =
            SimpleCache {
                songs.filter { s -> s.custom }
            }

    fun getCustomSongs(): List<Song> {
        return customSongsCache.get()
    }
}