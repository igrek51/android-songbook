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

    var customSongs: SimpleCache<List<Song>> =
            SimpleCache {
                songs.filter { s -> s.custom }
            }

    var generalCategories: SimpleCache<List<Category>> =
            SimpleCache {
                categories.filter { c -> c.type != CategoryType.CUSTOM }
            }

}