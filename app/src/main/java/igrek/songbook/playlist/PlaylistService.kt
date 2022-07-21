package igrek.songbook.playlist

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.playlist.Playlist
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songpreview.SongPreviewLayoutController

class PlaylistService(
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    contextMenuBuilder: LazyInject<ContextMenuBuilder> = appFactory.contextMenuBuilder,
    songOpener: LazyInject<SongOpener> = appFactory.songOpener,
    songPreviewLayoutController: LazyInject<SongPreviewLayoutController> = appFactory.songPreviewLayoutController,
) {
    private val songsRepository by LazyExtractor(songsRepository)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val contextMenuBuilder by LazyExtractor(contextMenuBuilder)
    private val songOpener by LazyExtractor(songOpener)
    private val songPreviewLayoutController by LazyExtractor(songPreviewLayoutController)

    fun showAddSongToPlaylistDialog(song: Song) {
        val playlists = songsRepository.playlistDao.playlistDb.playlists
        if (playlists.isEmpty()) {
            uiInfoService.showInfo(R.string.no_playlists_to_add)
            return
        }

        val actions = mutableListOf<ContextMenuBuilder.Action>()

        playlists.forEach { p ->
            val name = p.name
            val action = ContextMenuBuilder.Action(name) {
                addSongToPlaylist(p, song)
            }
            actions.add(action)
        }

        contextMenuBuilder.showContextMenu(R.string.choose_playlist, actions)
    }

    private fun addSongToPlaylist(playlist: Playlist, song: Song) {
        if (songsRepository.playlistDao.isSongOnPlaylist(song, playlist)) {
            uiInfoService.showInfo(R.string.song_already_on_playlist, song.displayName(), playlist.name)
            return
        }
        songsRepository.playlistDao.addSongToPlaylist(song, playlist)
        uiInfoService.showInfo(R.string.song_added_to_playlist, song.displayName(), playlist.name)
    }

    fun removeFromPlaylist(song: Song) {
        val playlistsWithSong = songsRepository.playlistDao.playlistDb.playlists
                .filter { playlist ->
                    songsRepository.playlistDao.isSongOnPlaylist(song, playlist)
                }

        when (playlistsWithSong.size) {
            0 -> {
                uiInfoService.showInfo(R.string.song_is_not_on_playlist)
                return
            }
            1 -> removeFromPlaylist(song, playlistsWithSong.first())
            else -> showRemoveSongFromPlaylistDialog(song, playlistsWithSong.toMutableList())
        }
    }

    private fun showRemoveSongFromPlaylistDialog(song: Song, playlists: MutableList<Playlist>) {
        val actions = mutableListOf<ContextMenuBuilder.Action>()

        playlists.forEach { playlist ->
            val name = playlist.name
            val action = ContextMenuBuilder.Action(name) {
                removeFromPlaylist(song, playlist)
            }
            actions.add(action)
        }

        contextMenuBuilder.showContextMenu(R.string.choose_playlist, actions)
    }

    private fun removeFromPlaylist(song: Song, playlist: Playlist) {
        songsRepository.playlistDao.removeSongFromPlaylist(song, playlist)
        uiInfoService.showInfo(R.string.song_removed_from_playlist, song.displayName(), playlist.name)
    }

    fun goToNextOrPrevious(next: Int) {
        val currentSong = songPreviewLayoutController.currentSong ?: return
        val playlist = songOpener.playlist ?: return
        val songIndex = findSongInPlaylist(currentSong, playlist)
        if (songIndex == -1)
            return
        val nextIndex = songIndex + next
        if (nextIndex < 0) {
            uiInfoService.showToast(R.string.playlist_at_beginning)
            return
        }
        if (nextIndex >= playlist.songs.size) {
            uiInfoService.showToast(R.string.playlist_at_end)
            return
        }
        val nextPlaylistSong = playlist.songs[nextIndex]
        val namespace = when {
            nextPlaylistSong.custom -> SongNamespace.Custom
            else -> SongNamespace.Public
        }
        val songId = SongIdentifier(nextPlaylistSong.songId, namespace)
        val nextSong = songsRepository.allSongsRepo.songFinder.find(songId) ?: return
        songOpener.openSongPreview(nextSong, playlist=playlist)
    }

    fun hasNextSong(): Boolean {
        val currentSong = songPreviewLayoutController.currentSong ?: return false
        val playlist = songOpener.playlist ?: return false
        val songIndex = findSongInPlaylist(currentSong, playlist)
        if (songIndex == -1)
            return false
        return songIndex + 1 < playlist.songs.size
    }

    private fun findSongInPlaylist(song: Song, playlist: Playlist): Int {
        return playlist.songs.indexOfFirst { s -> s.songId == song.id && s.custom == song.isCustom() }
    }

    fun getSongsFromPlaylist(playlist: Playlist): MutableList<Song> {
        return playlist.songs
            .mapNotNull { s ->
                val namespace = when {
                    s.custom -> SongNamespace.Custom
                    else -> SongNamespace.Public
                }
                val id = SongIdentifier(s.songId, namespace)
                val song = songsRepository.allSongsRepo.songFinder.find(id)
                song
            }
            .toMutableList()
    }

}