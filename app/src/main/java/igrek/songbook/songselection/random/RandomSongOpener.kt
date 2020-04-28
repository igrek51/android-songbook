package igrek.songbook.songselection.random

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.favourite.FavouriteSongsService
import java.util.*

class RandomSongOpener(
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
        favouriteSongsService: LazyInject<FavouriteSongsService> = appFactory.favouriteSongsService,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        songOpener: LazyInject<SongOpener> = appFactory.songOpener,
        preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
        appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
) {
    private val songsRepository by LazyExtractor(songsRepository)
    private val favouriteSongsService by LazyExtractor(favouriteSongsService)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songOpener by LazyExtractor(songOpener)
    private val preferencesState by LazyExtractor(preferencesState)
    private val appLanguageService by LazyExtractor(appLanguageService)

    private var fromFavouriteSongsOnly: Boolean
        get() = preferencesState.randomFavouriteSongsOnly
        set(value) {
            preferencesState.randomFavouriteSongsOnly = value
        }

    fun openRandomSong() {
        val randomSong = getRandomSong()
        if (randomSong == null) {
            uiInfoService.showInfo(R.string.no_songs_to_shuffle)
            return
        }

        songOpener.openSongPreview(randomSong)
    }

    private fun getRandomSong(): Song? {
        val songsCollection = songsToShuffle()
        if (songsCollection.isEmpty())
            return null
        return songsCollection[Random().nextInt(songsCollection.size)]
    }

    private fun songsToShuffle(): List<Song> {
        return if (fromFavouriteSongsOnly) {
            favouriteSongsService.getFavouriteSongs().toList()
        } else {
            val acceptedLanguages = appLanguageService.selectedSongLanguages
            val acceptedLangCodes = acceptedLanguages.map { lang -> lang.langCode } + "" + null
            songsRepository.allSongsRepo.songs.get()
                    .filter { song -> song.language in acceptedLangCodes }
        }
    }
}
