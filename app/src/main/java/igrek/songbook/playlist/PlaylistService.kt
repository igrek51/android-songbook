package igrek.songbook.playlist

import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.playlist.PlaylistSong
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

    fun showAddSongToPlaylist(song: Song) {
        val playlists = songsRepository.playlistDao.playlistDb.playlists
        if (playlists.isEmpty()) {
            uiInfoService.showInfo(R.string.no_playlists_to_add)
            return
        }

        val actions = mutableListOf<ContextMenuBuilder.Action>()

        playlists.forEach { p ->
            val name = p.name
            val action = ContextMenuBuilder.Action(name) {
                p.songs.add(PlaylistSong(song.id, song.custom))
                uiInfoService.showInfo(R.string.song_added_to_playlist)
            }
            actions.add(action)
        }

        contextMenuBuilder.showContextMenu(R.string.choose_playlist, actions)
    }
}