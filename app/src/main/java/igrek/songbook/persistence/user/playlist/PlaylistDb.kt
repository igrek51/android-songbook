package igrek.songbook.persistence.user.playlist

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDb(
        var playlists: MutableList<Playlist> = mutableListOf()
)

@Serializable
data class Playlist(
        var id: Long = 0,
        var name: String,
        var songs: MutableList<PlaylistSong> = mutableListOf()
)

@Serializable
data class PlaylistSong(
        val songId: Long,
        val custom: Boolean
)
