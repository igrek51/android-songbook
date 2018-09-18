package igrek.songbook.domain.songsdb

import igrek.songbook.system.cache.SimpleCache

data class SongsDb(
        val versionNumber: Long,
        val categories: List<SongCategory>,
        val allSongs: List<Song>
) {
    private var unlockedSongsCache: SimpleCache<List<Song>> =
            SimpleCache { allSongs.filter { s -> !s.locked } }

    private var unlockedCategoriesCache: SimpleCache<List<SongCategory>> =
            SimpleCache { categories.filter { c -> c.getUnlockedSongs().any() } }

    fun getAllUnlockedSongs(): List<Song> {
        return unlockedSongsCache.get()
    }

    /*
     * get all nonempty categories with unlocked songs only
     */
    fun getAllUnlockedCategories(): List<SongCategory> {
        return unlockedCategoriesCache.get()
    }
}