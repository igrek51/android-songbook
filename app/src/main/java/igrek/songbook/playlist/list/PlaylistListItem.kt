package igrek.songbook.playlist.list

import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.user.playlist.Playlist

open class PlaylistListItem(
    val playlist: Playlist? = null,
    val song: Song? = null
) {

    override fun toString(): String {
        return when {
            playlist != null -> """[${playlist.name}]"""
            song != null -> song.displayName()
            else -> ""
        }
    }

}
