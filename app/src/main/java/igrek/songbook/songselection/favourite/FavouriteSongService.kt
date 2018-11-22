package igrek.songbook.songselection.favourite

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.model.songsdb.Song
import igrek.songbook.persistence.SongsRepository
import javax.inject.Inject

class FavouriteSongService {

    @Inject
    lateinit var songsRepository: SongsRepository

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun isSongFavourite(song: Song): Boolean {
        return songsRepository.getFavouriteSongs().contains(song)
    }

    fun getFavouriteSongs(): Set<Song> {
        return songsRepository.getFavouriteSongs()
    }

    fun setSongFavourite(song: Song) {
        songsRepository.setSongFavourite(song)
    }

    fun unsetSongFavourite(song: Song) {
        songsRepository.unsetSongFavourite(song)
    }

}