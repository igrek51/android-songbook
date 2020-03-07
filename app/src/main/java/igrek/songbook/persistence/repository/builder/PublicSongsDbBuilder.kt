package igrek.songbook.persistence.repository.builder

import igrek.songbook.info.UiResourceService
import igrek.songbook.persistence.general.dao.PublicSongsDao
import igrek.songbook.persistence.general.model.*
import igrek.songbook.persistence.repository.PublicSongsRepository
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.util.lookup.FinderById
import igrek.songbook.util.lookup.FinderByTuple
import igrek.songbook.util.lookup.SimpleCache

class PublicSongsDbBuilder(
        private val versionNumber: Long,
        private val publicSongsDao: PublicSongsDao,
        private val userDataDao: UserDataDao) {

    fun buildPublic(uiResourceService: UiResourceService): PublicSongsRepository {
        val categories: MutableList<Category> = publicSongsDao.readAllCategories()
        val songs: MutableList<Song> = publicSongsDao.readAllSongs()
        val songCategories: MutableList<SongCategoryRelationship> = publicSongsDao.readAllSongCategories()

        unlockSongs(songs)
        removeLockedSongs(songs)
        excludeSongs(categories, songs)
        assignSongsToCategories(categories, songs, songCategories)
        pruneEmptyCategories(categories)

        refillCategoryDisplayNames(uiResourceService, categories)

        return PublicSongsRepository(versionNumber, SimpleCache { categories }, SimpleCache { songs })
    }

    private fun refillCategoryDisplayNames(uiResourceService: UiResourceService, categories: List<Category>) {
        categories.forEach { category ->
            category.displayName = when {
                category.type.localeStringId != null ->
                    uiResourceService.resString(category.type.localeStringId)
                else -> category.name
            }
        }
    }

    private fun excludeSongs(categories: MutableList<Category>, songs: MutableList<Song>) {
        userDataDao.exclusionDao!!.setAllArtists(categories)

        val excludedArtistIds = userDataDao.exclusionDao!!.exclusionDb.artistIds
        categories.removeAll { category -> category.id in excludedArtistIds }

        val excludedLanguages = userDataDao.exclusionDao!!.exclusionDb.languages
        songs.removeAll { song -> song.language?.let { it in excludedLanguages } ?: false }
    }

    private fun unlockSongs(songs: MutableList<Song>) {
        val keys = userDataDao.unlockedSongsDao!!.unlockedSongs.keys
        songs.forEach { song ->
            if (song.locked && keys.contains(song.lockPassword)) {
                song.locked = false
            }
        }
    }

    private fun removeLockedSongs(songs: MutableList<Song>) {
        songs.removeAll { song -> song.locked }
    }

    private fun pruneEmptyCategories(categories: MutableList<Category>) {
        categories.removeAll { category -> category.songs.isEmpty() }
    }

    private fun assignSongsToCategories(categories: MutableList<Category>, songs: MutableList<Song>, songCategories: MutableList<SongCategoryRelationship>) {
        val songFinder = FinderByTuple(songs) { song -> song.songIdentifier() }
        val categoryFinder = FinderById(categories) { e -> e.id }

        songCategories.forEach { scRelation ->
            val song = songFinder.find(SongIdentifier(scRelation.song_id, SongNamespace.Public))
            val category = categoryFinder.find(scRelation.category_id)
            if (song != null && category != null) {
                song.categories.add(category)
                category.songs.add(song)
            }
        }
    }

}