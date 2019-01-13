package igrek.songbook.songselection.random

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.layout.LayoutController
import igrek.songbook.model.songsdb.Song
import igrek.songbook.persistence.SongsRepository
import igrek.songbook.settings.preferences.PreferencesDefinition
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.songpreview.SongPreviewLayoutController
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

    var fromFavouriteSongsOnly: Boolean = false

    init {
        DaggerIoc.getFactoryComponent().inject(this)
        loadPreferences()
    }

    private fun loadPreferences() {
        fromFavouriteSongsOnly = preferencesService.getValue(PreferencesDefinition.randomFavouriteSongsOnly, Boolean::class.java)!!
    }

    fun openRandomSong() {
        val randomSong = getRandomSong()
        if (randomSong != null)
            openSongPreview(randomSong)
    }

    private fun getRandomSong(): Song? {
        val allSongs = songsRepository.songsDb!!.getAllUnlockedSongs()
        if (allSongs.isEmpty())
            return null
        return allSongs[Random().nextInt(allSongs.size)]
    }

    private fun openSongPreview(song: Song) {
        songPreviewLayoutController.get().setCurrentSong(song)
        layoutController.showSongPreview()
    }
}
