package igrek.songbook.persistence.repository

import igrek.songbook.persistence.general.dao.GeneralSongsDao
import igrek.songbook.persistence.general.model.*
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.persistence.user.custom.CustomSongMapper
import igrek.songbook.util.lookup.FinderById
import igrek.songbook.util.lookup.FinderByTuple

class SongsDbBuilder(
        private val versionNumber: Long,
        private val generalSongsDao: GeneralSongsDao,
        private val userDataDao: UserDataDao) {

    private var categories = mutableListOf<Category>()
    private var songs = mutableListOf<Song>()

    fun build(): SongsDb {
        categories = generalSongsDao.readAllCategories()
        songs = generalSongsDao.readAllSongs()

        // user data
        applyCustomSongs()
        unlockSongs()

        removeLockedSongs()
        assignSongsToCategories()
        pruneEmptyCategories()

        return SongsDb(versionNumber, categories, songs)
    }

    private fun applyCustomSongs() {
        val customSongs = userDataDao.customSongsDao!!.customSongs.songs
        val categoryFinder = FinderById(categories) { e -> e.id }
        val customCategory = categoryFinder.find(CategoryType.CUSTOM.id)!!
        val mapper = CustomSongMapper()

        customSongs.forEach { customSong ->
            val song = mapper.customSongToSong(customSong)
            songs.add(song)
            song.categories = mutableListOf(customCategory)
            customCategory.songs.add(song)
        }
    }

    private fun unlockSongs() {
        val keys = userDataDao.unlockedSongsDao!!.unlockedSongs.keys
        songs.forEach { song ->
            if (song.locked && keys.contains(song.lockPassword)) {
                song.locked = false
            }
        }
    }

    private fun removeLockedSongs() {
        songs = songs.filter { song -> !song.locked }.toMutableList()
    }

    private fun pruneEmptyCategories() {
        categories = categories.filter { category -> category.songs.isNotEmpty() }.toMutableList()
    }

    private fun assignSongsToCategories() {
        val songCategories = generalSongsDao.readAllSongCategories()

        val songFinder = FinderByTuple(songs) { song -> song.songIdentifier() }
        val categoryFinder = FinderById(categories) { e -> e.id }

        songCategories.forEach { scRelation ->
            val song = songFinder.find(SongIdentifier(scRelation.song_id, false))
            val category = categoryFinder.find(scRelation.category_id)
            if (song != null && category != null) {
                song.categories.add(category)
                category.songs.add(song)
            }
        }
    }

}