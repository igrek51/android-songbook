package igrek.songbook.songpreview

import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.history.OpenedSong
import javax.inject.Inject

class SongOpener {

    @Inject
    lateinit var layoutController: Lazy<LayoutController>
    @Inject
    lateinit var songPreviewLayoutController: Lazy<SongPreviewLayoutController>
    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var uiInfoService: UiInfoService

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun openSongPreview(song: Song) {
        songPreviewLayoutController.get().currentSong = song
        layoutController.get().showLayout(SongPreviewLayoutController::class)
        songsRepository.openHistoryDao.registerOpenedSong(song.id, song.isCustom())
    }

    fun openLastSong() {
        if (songPreviewLayoutController.get().currentSong != null) {
            layoutController.get().showLayout(SongPreviewLayoutController::class)
            return
        }

        val openedSong: OpenedSong? = songsRepository.openHistoryDao.historyDb.songs.firstOrNull()
        if (openedSong != null) {
            val namespace = when {
                openedSong.custom -> SongNamespace.Custom
                else -> SongNamespace.Public
            }
            val songIdentifier = SongIdentifier(openedSong.songId, namespace)
            val song: Song? = songsRepository.allSongsRepo.songFinder.find(songIdentifier)
            if (song != null) {
                openSongPreview(song)
                return
            }
        }

        uiInfoService.showInfo(R.string.no_last_song)
    }
}