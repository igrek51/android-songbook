package igrek.songbook.model.songsdb

import igrek.songbook.system.cache.SimpleCache

data class SongsDb(
        val versionNumber: Long,
        val categories: List<SongCategory>,
        val allSongs: List<Song>
) {
    private var unlockedSongsCache: SimpleCache<List<Song>> =
            SimpleCache {
                allSongs.filter { s -> !s.locked }
            }

    private var publicUnlockedCategoriesCache: SimpleCache<List<SongCategory>> =
            SimpleCache {
                categories.filter { c -> c.type != SongCategoryType.CUSTOM }
                        .filter { c -> c.getUnlockedSongs().any() }
            }

    private var unlockedCategoriesCache: SimpleCache<List<SongCategory>> =
            SimpleCache {
                categories.filter { c -> c.getUnlockedSongs().any() }
            }

    private var customSongsCache: SimpleCache<List<Song>> =
            SimpleCache {
                allSongs.filter { s -> s.category.type == SongCategoryType.CUSTOM }
            }


    fun getAllUnlockedSongs(): List<Song> {
        return unlockedSongsCache.get()
    }

    /*
     * get all nonempty categories with unlocked songs only
     */
    fun getAllUnlockedCategories(): List<SongCategory> {
        return unlockedCategoriesCache.get()
    }

    fun getPublicUnlockedCategories(): List<SongCategory> {
        return publicUnlockedCategoriesCache.get()
    }

    fun getCustomSongs(): List<Song> {
        return customSongsCache.get()
    }
}