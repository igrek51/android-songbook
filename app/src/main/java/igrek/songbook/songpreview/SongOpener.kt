package igrek.songbook.songpreview

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.analytics.AnalyticsLogger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.history.OpenedSong

class SongOpener(
        layoutController: LazyInject<LayoutController> = appFactory.layoutController,
        songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) {
    private val layoutController by LazyExtractor(layoutController)
    private val songPreviewLayoutController by LazyExtractor(songPreviewLayoutController)
    private val songsRepository by LazyExtractor(songsRepository)
    private val uiInfoService by LazyExtractor(uiInfoService)

    fun openSongPreview(song: Song) {
        songPreviewLayoutController.currentSong = song
        layoutController.showLayout(SongPreviewLayoutController::class)
        songsRepository.openHistoryDao.registerOpenedSong(song.id, song.isCustom())
        AnalyticsLogger().logEventSongOpened(song)
    }

    fun openLastSong() {
        if (songPreviewLayoutController.currentSong != null) {
            layoutController.showLayout(SongPreviewLayoutController::class)
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