package igrek.songbook.persistence.repository

import igrek.songbook.persistence.general.dao.GeneralSongsDao
import igrek.songbook.persistence.general.model.*
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.persistence.user.custom.CustomCategory
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

        applyCustomSongs()
        unlockSongs()
        removeLockedSongs()
        excludeSongs()
        assignSongsToCategories()
        pruneEmptyCategories()

        return SongsDb(versionNumber, categories, songs)
    }

    private fun excludeSongs() {
        userDataDao.exclusionDao!!.setAllArtists(categories)

        val excludedArtistIds = userDataDao.exclusionDao!!.exclusionDb.artistIds
        categories = categories
                .filter { category -> category.id !in excludedArtistIds }
                .toMutableList()

        val excludedLanguages = userDataDao.exclusionDao!!.exclusionDb.languages
        songs = songs
                .filter { song -> !(song.language?.let { it in excludedLanguages } ?: false) }
                .toMutableList()
    }

    private fun applyCustomSongs() {
        val customSongs = userDataDao.customSongsDao!!.customSongs.songs
        val categoryFinder = FinderById(categories) { e -> e.id }
        val customGeneralCategory = categoryFinder.find(CategoryType.CUSTOM.id)!!
        val mapper = CustomSongMapper()

        // bind custom categories to songs
        userDataDao.customSongsDao!!.customCategories = customSongs.map { song ->
            song.categoryName
        }.toSet().filterNotNull().map { categoryName ->
            CustomCategory(name = categoryName)
        }
        val customCategoryFinder = FinderByTuple(userDataDao.customSongsDao!!.customCategories) {
            it.name
        }

        customSongs.forEach { customSong ->
            val song = mapper.customSongToSong(customSong)
            songs.add(song)

            song.categories = mutableListOf(customGeneralCategory)
            customGeneralCategory.songs.add(song)

            val customCategory = customCategoryFinder.find(customSong.categoryName ?: "")
            customCategory?.songs?.add(song)
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
        categories = categories
                .filter { category -> category.songs.isNotEmpty() }
                .toMutableList()
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