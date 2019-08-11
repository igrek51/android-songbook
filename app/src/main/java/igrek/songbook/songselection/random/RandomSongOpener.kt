package igrek.songbook.songselection.random

import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.preferences.PreferencesDefinition
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songpreview.SongPreviewLayoutController
import igrek.songbook.songselection.favourite.FavouriteSongsService
import java.util.*
import javax.inject.Inject

class RandomSongOpener {

    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var songPreviewLayoutController: dagger.Lazy<SongPreviewLayoutController>
    @Inject
    lateinit var preferencesService: PreferencesService
    @Inject
    lateinit var favouriteSongsService: FavouriteSongsService
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var songOpener: SongOpener

    var fromFavouriteSongsOnly: Boolean = false

    init {
        DaggerIoc.factoryComponent.inject(this)
        loadPreferences()
    }

    private fun loadPreferences() {
        fromFavouriteSongsOnly = preferencesService.getValue(PreferencesDefinition.randomFavouriteSongsOnly, Boolean::class.java)!!
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
            songsRepository.songsDb!!.songs
        }
    }
}
