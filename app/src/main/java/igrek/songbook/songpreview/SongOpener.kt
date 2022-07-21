package igrek.songbook.songpreview

import igrek.songbook.R
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
import igrek.songbook.room.RoomLobby

open class SongOpener(
    layoutController: LazyInject<LayoutController> = appFactory.layoutController,
    songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    roomLobby: LazyInject<RoomLobby> = appFactory.roomLobby,
) {
    private val layoutController by LazyExtractor(layoutController)
    private val songPreviewLayoutController by LazyExtractor(songPreviewLayoutController)
    private val songsRepository by LazyExtractor(songsRepository)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val roomLobby by LazyExtractor(roomLobby)

    var playlist: Playlist? = null
        private set

    fun openSongPreview(song: Song, playlist: Playlist? = null) {
        logger.info("Opening song: $song")
        this.playlist = playlist
        songPreviewLayoutController.currentSong = song
        layoutController.showLayout(SongPreviewLayoutController::class)
        songsRepository.openHistoryDao.registerOpenedSong(song.id, song.namespace)
        roomLobby.reportSongSelected(song)
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
        this.playlist = null
        val currentSong = songPreviewLayoutController.currentSong
        if (currentSong != null) {
            layoutController.showLayout(SongPreviewLayoutController::class)
            roomLobby.reportSongSelected(currentSong)
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

    fun isPlaylistOpen(): Boolean {
        return playlist != null
    }

    fun hasLastSong(): Boolean {
        songsRepository.openHistoryDao.historyDb.songs.firstOrNull()?.let{ openedSong: OpenedSong ->
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