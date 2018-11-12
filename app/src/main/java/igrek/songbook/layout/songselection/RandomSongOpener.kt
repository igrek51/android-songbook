package igrek.songbook.layout.songselection

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.domain.songsdb.Song
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.songpreview.SongPreviewLayoutController
import igrek.songbook.persistence.SongsRepository
import java.util.*
import javax.inject.Inject

class RandomSongOpener {

    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var songPreviewLayoutController: dagger.Lazy<SongPreviewLayoutController>

    init {
        DaggerIoc.getFactoryComponent().inject(this)
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
