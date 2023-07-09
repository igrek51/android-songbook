package igrek.songbook.playlist

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.InputDialogBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.general.model.SongIdentifier
import igrek.songbook.persistence.general.model.SongNamespace
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.playlist.Playlist
import igrek.songbook.persistence.user.playlist.PlaylistSong
import igrek.songbook.songpreview.SongOpener
import igrek.songbook.songpreview.SongPreviewLayoutController
import io.reactivex.subjects.PublishSubject

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

    var currentPlaylist: Playlist? = null
    var addPlaylistSongSubject: PublishSubject<Pair<Song, Boolean>> = PublishSubject.create()

    fun addNewPlaylist(onSuccess: (Playlist) -> Unit = {}) {
        InputDialogBuilder().input(R.string.new_playlist_name, null) { name ->
            if (name.isNotBlank()) {
                val playlist = Playlist(0, name)
                songsRepository.playlistDao.savePlaylist(playlist)
                uiInfoService.showInfo(R.string.playlist_created, name)
                onSuccess(playlist)
            }
        }
    }

    fun showAddSongToPlaylistDialog(song: Song) {
        val playlists = songsRepository.playlistDao.playlistDb.playlists
        if (playlists.isEmpty()) {
            uiInfoService.showToast(R.string.no_playlists_to_add)
            addNewPlaylist { playlist ->
                addSongToPlaylist(playlist, song)
            }
            return
        }

        val actions = mutableListOf<ContextMenuBuilder.Action>()

        playlists.forEach { playlist ->
            val name = playlist.name
            val action = ContextMenuBuilder.Action(name) {
                addSongToPlaylist(playlist, song)
            }
            actions.add(action)
        }

        contextMenuBuilder.showContextMenu(R.string.choose_playlist, actions)
    }

    private fun addSongToPlaylist(playlist: Playlist, song: Song) {
        if (songsRepository.playlistDao.isSongOnPlaylist(song, playlist)) {
            uiInfoService.showInfo(
                R.string.song_already_on_playlist,
                song.displayName(),
                playlist.name
            )
            return
        }
        songsRepository.playlistDao.addSongToPlaylist(song, playlist)
        uiInfoService.showInfo(R.string.song_added_to_playlist, song.displayName(), playlist.name)
    }

    fun addSongToCurrentPlaylist(song: Song) {
        currentPlaylist?.let { currentPlaylist ->
            addSongToPlaylist(currentPlaylist, song)
            addPlaylistSongSubject.onNext(song to true)
        } ?: kotlin.run {
            uiInfoService.showInfo(R.string.playlist_not_selected)
        }
    }

    fun removeFromThisPlaylist(song: Song) {
        currentPlaylist?.let { currentPlaylist ->
            removeFromPlaylist(song, currentPlaylist)
        } ?: kotlin.run {
            uiInfoService.showInfo(R.string.song_is_not_on_playlist)
        }
    }

    fun toggleSongInCurrentPlaylist(song: Song) {
        currentPlaylist?.let { currentPlaylist ->
            if (songsRepository.playlistDao.isSongOnPlaylist(song, currentPlaylist)) {
                removeFromPlaylist(song, currentPlaylist)
                addPlaylistSongSubject.onNext(song to false)
            } else {
                addSongToPlaylist(currentPlaylist, song)
                addPlaylistSongSubject.onNext(song to true)
            }
        } ?: kotlin.run {
            uiInfoService.showInfo(R.string.playlist_not_selected)
        }
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
        uiInfoService.showInfo(
            R.string.song_removed_from_playlist,
            song.displayName(),
            playlist.name
        )
    }

    fun goToNextOrPrevious(next: Int): Boolean {
        val currentSong = songPreviewLayoutController.currentSong ?: return false
        val playlist = currentPlaylist ?: return false
        val songIndex = findSongInPlaylist(currentSong, playlist)
        if (songIndex == -1)
            return false
        val nextIndex = songIndex + next
        if (nextIndex < 0) {
            uiInfoService.showInfo(R.string.playlist_at_beginning)
            return false
        }
        if (nextIndex >= playlist.songs.size) {
            uiInfoService.showInfo(R.string.playlist_at_end)
            return false
        }
        val nextPlaylistSong = playlist.songs[nextIndex]
        val namespace = when {
            nextPlaylistSong.custom -> SongNamespace.Custom
            else -> SongNamespace.Public
        }
        val songId = SongIdentifier(nextPlaylistSong.songId, namespace)
        val nextSong = songsRepository.allSongsRepo.songFinder.find(songId) ?: return false
        songOpener.openSongPreview(nextSong, playlist = playlist)
        when {
            next > 0 -> uiInfoService.showInfo(R.string.next_playlist_song_opened)
            next < 0 -> uiInfoService.showInfo(R.string.previous_playlist_song_opened)
        }
        return true
    }

    fun hasNextSong(): Boolean {
        val currentSong = songPreviewLayoutController.currentSong ?: return false
        val playlist = currentPlaylist ?: return false
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

    fun isPlaylistOpen(): Boolean {
        return currentPlaylist != null
    }

    fun isSongOnCurrentPlaylist(song: Song): Boolean {
        return currentPlaylist?.let { currentPlaylist ->
            isSongOnPlaylist(song, currentPlaylist)
        } ?: false
    }

    private fun isSongOnPlaylist(song: Song, playlist: Playlist): Boolean {
        val playlistSong = PlaylistSong(song.id, song.isCustom())
        return playlistSong in playlist.songs
    }

}