package igrek.songbook.songpreview

import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import javax.inject.Inject

class SongOpener {

    @Inject
    lateinit var layoutController: dagger.Lazy<LayoutController>
    @Inject
    lateinit var songPreviewLayoutController: dagger.Lazy<SongPreviewLayoutController>
    @Inject
    lateinit var songsRepository: SongsRepository

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun openSongPreview(song: Song) {
        songPreviewLayoutController.get().currentSong = song
        layoutController.get().showLayout(SongPreviewLayoutController::class)
        songsRepository.openHistoryDao.registerOpenedSong(song.id, song.custom)
    }
}