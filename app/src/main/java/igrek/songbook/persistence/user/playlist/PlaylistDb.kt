package igrek.songbook.persistence.user.playlist

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDb(
        val playlists: MutableList<Playlist>
)

@Serializable
data class Playlist(
        val name: String,
        val songs: MutableList<PlaylistSong>
)

@Serializable
data class PlaylistSong(
        val songId: Long,
        val custom: Boolean
)
