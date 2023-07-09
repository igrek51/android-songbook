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
import igrek.songbook.util.defaultScope
import kotlinx.coroutines.launch

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

    var lastSongWasRandom: Boolean = false
        private set

    fun openSongPreview(
        song: Song,
        playlist: Playlist? = null,
        isRandom: Boolean = false,
        onInit: (() -> Unit)? = null,
    ) {
        logger.info("Opening song: $song")
        playlistService.currentPlaylist = playlist
        songPreviewLayoutController.currentSong = song
        lastSongWasRandom = isRandom
        onInit?.let {
            songPreviewLayoutController.addOnInitListener(onInit)
        }
        defaultScope.launch {
            songCastService.presentMyOpenedSong(song)
        }
        showSongPreview()
        songsRepository.openHistoryDao.registerOpenedSong(song.id, song.namespace)
        AnalyticsLogger().logEventSongOpened(song)
    }

    fun openLastSong() {
        playlistService.currentPlaylist = null
        lastSongWasRandom = false
        songPreviewLayoutController.currentSong?.let { currentSong ->
            defaultScope.launch {
                songCastService.presentMyOpenedSong(currentSong)
            }
            showSongPreview()
            return
        }

        val openedSong: OpenedSong = songsRepository.openHistoryDao.historyDb.songs.firstOrNull()
            ?: run {
                uiInfoService.showInfo(R.string.no_last_song)
                return
            }

        val namespace = when {
            openedSong.custom -> SongNamespace.Custom
            else -> SongNamespace.Public
        }
        val songIdentifier = SongIdentifier(openedSong.songId, namespace)
        val opened = openSongIdentifier(songIdentifier)
        if (!opened) {
            uiInfoService.showInfo(R.string.no_last_song)
        }
    }

    private fun openSongIdentifier(songIdentifier: SongIdentifier): Boolean {
        songsRepository.allSongsRepo.songFinder.find(songIdentifier)?.let { song ->
            openSongPreview(song)
            return true
        }
        return false
    }

    private fun showSongPreview() {
        layoutController.showLayout(SongPreviewLayoutController::class)
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