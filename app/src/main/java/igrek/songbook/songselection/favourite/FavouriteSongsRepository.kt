package igrek.songbook.songselection.favourite

import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.persistence.FavouriteSongsDao
import igrek.songbook.persistence.SongsRepository
import igrek.songbook.persistence.songsdb.Song
import igrek.songbook.system.cache.SimpleCache
import java.util.*
import javax.inject.Inject

class FavouriteSongsRepository {

    @Inject
    lateinit var songsRepository: dagger.Lazy<SongsRepository>
    @Inject
    lateinit var favouriteSongsDao: FavouriteSongsDao
    @Inject
    lateinit var uiInfoService: UiInfoService

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
        uiInfoService.showInfo(R.string.favourite_song_has_been_set)
    }

    fun unsetSongFavourite(song: Song) {
        favouriteSongsDao.unsetFavourite(song)
        favouritesCache.get().remove(song)
        uiInfoService.showInfo(R.string.favourite_song_has_been_unset)
    }

    fun resetCache() {
        favouritesCache.reset()
    }

}