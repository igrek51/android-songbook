package igrek.songbook.songpreview

import igrek.songbook.R
import igrek.songbook.cast.SongCastService
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.analytics.AnalyticsLogger
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.history.OpenedSong
import igrek.songbook.persistence.user.playlist.Playlist
import igrek.songbook.playlist.PlaylistService

open class SongOpener(
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    songCastService: LazyInject<SongCastService> = appFactory.songCastService,
    playlistService: LazyInject<PlaylistService> = appFactory.playlistService,
) {
    private val layoutController by LazyExtractor(layoutController)
    private val songPreviewLayoutController by LazyExtractor(songPreviewLayoutController)
    private val songsRepository by LazyExtractor(songsRepository)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val songCastService by LazyExtractor(songCastService)
    private val playlistService by LazyExtractor(playlistService)

    fun openSongPreview(song: Song, playlist: Playlist? = null, onInit: (() -> Unit)? = null) {
        logger.info("Opening song: $song")
        playlistService.currentPlaylist = playlist
        songPreviewLayoutController.currentSong = song
        onInit?.let {
            songPreviewLayoutController.addOnInitListener(onInit)
        }
        songCastService.presentMyOpenedSong(song)
        layoutController.showLayout(SongPreviewLayoutController::class)
        songsRepository.openHistoryDao.registerOpenedSong(song.id, song.namespace)
        AnalyticsLogger().logEventSongOpened(song)
    }

    private fun openSongIdentifier(songIdentifier: SongIdentifier): Boolean {
        songsRepository.allSongsRepo.songFinder.find(songIdentifier)?.let { song ->
            openSongPreview(song)
            return true
        }
        return false
    }

    fun openLastSong() {
        playlistService.currentPlaylist = null
        val currentSong = songPreviewLayoutController.currentSong
        if (currentSong != null) {
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
            if (openSongIdentifier(songIdentifier))
                return
        }

        uiInfoService.showInfo(R.string.no_last_song)
    }

    fun hasLastSong(): Boolean {
        songsRepository.openHistoryDao.historyDb.songs.firstOrNull()
            ?.let { openedSong: OpenedSong ->
                val namespace = when {
                    openedSong.custom -> SongNamespace.Custom
                    else -> SongNamespace.Public
                }
                val songIdentifier = SongIdentifier(openedSong.songId, namespace)
                val song = songsRepository.allSongsRepo.songFinder.find(songIdentifier)
                if (song != null)
                    return true
            }
        return false
    }
}