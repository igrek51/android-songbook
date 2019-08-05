package igrek.songbook.persistence

import igrek.songbook.persistence.dao.SongsDao
import igrek.songbook.persistence.model.Category
import igrek.songbook.persistence.model.Song
import igrek.songbook.persistence.model.SongsDb
import igrek.songbook.persistence.user.UserDataService
import igrek.songbook.system.cache.FinderById

class SongsDbBuilder(private val songsDao: SongsDao, private val userDataService: UserDataService) {

    fun build(): SongsDb {
        val versionNumber = songsDao.readDbVersionNumber()
                ?: throw RuntimeException("invalid songs database format")
        var categories = songsDao.readAllCategories()
        var songs = songsDao.readAllSongs()

        loadUserData()

        songs = removeLockedSongs(songs)
        assignSongsToCategories(songs, categories)
        categories = pruneEmptyCategories(categories)

        return SongsDb(versionNumber, categories, songs)
    }

    private fun loadUserData() {
        // TODO latest data first, then migrate olders
        //        loadCustomSongs(songsDb)
        //        loadFavourites(songsDb)
        //        unlockSongs(songsDb)
        //        loadPlaylists(songsDb)
    }

    private fun removeLockedSongs(songs: List<Song>): MutableList<Song> {
        return songs.filter { song -> !song.locked }.toMutableList()
    }

    private fun pruneEmptyCategories(categories: List<Category>): List<Category> {
        return categories.filter { category -> category.songs.isNotEmpty() }
    }

    private fun assignSongsToCategories(songs: List<Song>, categories: List<Category>) {
        val songCategories = songsDao.readAllSongCategories()

        val songFinder = FinderById(songs) { e -> e.id }
        val categoryFinder = FinderById(categories) { e -> e.id }

        songCategories.forEach { scRelation ->
            val song = songFinder.find(scRelation.song_id)
            val category = categoryFinder.find(scRelation.category_id)
            if (song != null && category != null) {
                song.categories.add(category)
                category.songs.add(song)
            }
        }
    }

}