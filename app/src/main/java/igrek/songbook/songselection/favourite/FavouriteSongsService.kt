package igrek.songbook.songselection.favourite

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.analytics.AnalyticsLogger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import io.reactivex.subjects.PublishSubject

class FavouriteSongsService(
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) {
    private val songsRepository by LazyExtractor(songsRepository)
    private val uiInfoService by LazyExtractor(uiInfoService)

    var updateFavouriteSongSubject: PublishSubject<Song> = PublishSubject.create()

    fun isSongFavourite(song: Song): Boolean {
        return songsRepository.favouriteSongsDao.isSongFavourite(song.songIdentifier())
    }

    fun getFavouriteSongs(): Set<Song> {
        return songsRepository.favouriteSongsDao.getFavouriteSongs()
    }

    fun setSongFavourite(song: Song) {
        songsRepository.favouriteSongsDao.setSongFavourite(song)
        uiInfoService.showInfo(R.string.favourite_song_has_been_set)
        updateFavouriteSongSubject.onNext(song)
        AnalyticsLogger().logEventSongFavourited(song)
    }

    fun unsetSongFavourite(song: Song) {
        songsRepository.favouriteSongsDao.unsetSongFavourite(song)
        uiInfoService.showInfo(R.string.favourite_song_has_been_unset)
        updateFavouriteSongSubject.onNext(song)
    }

}