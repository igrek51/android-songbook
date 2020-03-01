package igrek.songbook.songselection.random

import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songselection.favourite.FavouriteSongsService
import java.util.*
import javax.inject.Inject

class RandomSongOpener {

    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var preferencesService: PreferencesService
    @Inject
    lateinit var favouriteSongsService: FavouriteSongsService
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var songOpener: SongOpener
    @Inject
    lateinit var preferencesState: PreferencesState

    private var fromFavouriteSongsOnly: Boolean
        get() = preferencesState.randomFavouriteSongsOnly
        set(value) {
            preferencesState.randomFavouriteSongsOnly = value
        }

    init {
        DaggerIoc.factoryComponent.inject(this)
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
            songsRepository.allSongsRepo.songs.get()
        }
    }
}
