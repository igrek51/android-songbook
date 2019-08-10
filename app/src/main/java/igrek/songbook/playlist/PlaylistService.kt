package igrek.songbook.playlist

import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.playlist.Playlist
import javax.inject.Inject

class PlaylistService {

    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var contextMenuBuilder: ContextMenuBuilder

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

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
            uiInfoService.showInfo(R.string.song_already_on_playlist)
            return
        }
        songsRepository.playlistDao.addSongToPlaylist(song, playlist)
        uiInfoService.showInfo(R.string.song_added_to_playlist)
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
        uiInfoService.showInfo(R.string.song_removed_from_playlist)
    }

}