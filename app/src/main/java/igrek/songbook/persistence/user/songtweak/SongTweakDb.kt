package igrek.songbook.persistence.user.songtweak

import kotlinx.serialization.Serializable

@Serializable
data class SongTweakDb(
    val songs: MutableList<TweakedSong> = mutableListOf()
)

@Serializable
data class TweakedSong(
    val songId: Long,
    val namespaceId: Long,
    var autoscrollSpeed: Float?,
)
