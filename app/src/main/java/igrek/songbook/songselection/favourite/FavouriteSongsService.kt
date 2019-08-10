package igrek.songbook.songselection.favourite

import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class FavouriteSongsService {

    @Inject
    lateinit var songsRepository: dagger.Lazy<SongsRepository>
    @Inject
    lateinit var uiInfoService: UiInfoService

    var updateFavouriteSongSubject: PublishSubject<Song> = PublishSubject.create()

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun isSongFavourite(song: Song): Boolean {
        return songsRepository.get().favouriteSongsDao.isSongFavourite(song.songIdentifier())
    }

    fun getFavouriteSongs(): Set<Song> {
        return songsRepository.get().favouriteSongsDao.getFavouriteSongs()
    }

    fun setSongFavourite(song: Song) {
        songsRepository.get().favouriteSongsDao.setSongFavourite(song)
        uiInfoService.showInfo(R.string.favourite_song_has_been_set)
        updateFavouriteSongSubject.onNext(song)
    }

    fun unsetSongFavourite(song: Song) {
        songsRepository.get().favouriteSongsDao.unsetSongFavourite(song)
        uiInfoService.showInfo(R.string.favourite_song_has_been_unset)
        updateFavouriteSongSubject.onNext(song)
    }

}