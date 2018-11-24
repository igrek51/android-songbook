package igrek.songbook.songselection.favourite

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.model.songsdb.Song
import igrek.songbook.persistence.FavouriteSongsDao
import igrek.songbook.persistence.SongsRepository
import igrek.songbook.system.cache.SimpleCache
import java.util.*
import javax.inject.Inject

class FavouriteSongsRepository {

    @Inject
    lateinit var songsRepository: dagger.Lazy<SongsRepository>
    @Inject
    lateinit var favouriteSongsDao: FavouriteSongsDao

    private var favouritesCache: SimpleCache<HashSet<Song>> =
            SimpleCache {
                val all = songsRepository.get().songsDb!!.getAllUnlockedSongs()
                val favourites = favouriteSongsDao.populateFavouriteSongs(all)
                HashSet(favourites)
            }

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun isSongFavourite(song: Song): Boolean {
        return getFavouriteSongs().contains(song)
    }

    fun getFavouriteSongs(): Set<Song> {
        return favouritesCache.get()
    }

    fun setSongFavourite(song: Song) {
        favouriteSongsDao.setAsFavourite(song)
        favouritesCache.get().add(song)
    }

    fun unsetSongFavourite(song: Song) {
        favouriteSongsDao.unsetFavourite(song)
        favouritesCache.get().remove(song)
    }

    fun resetCache() {
        favouritesCache.reset()
    }

}