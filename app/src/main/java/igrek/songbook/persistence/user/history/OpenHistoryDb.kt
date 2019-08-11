package igrek.songbook.persistence.user.history

import kotlinx.serialization.Serializable

@Serializable
data class OpenHistoryDb(
        var songs: MutableList<OpenedSong> = mutableListOf()
)

@Serializable
data class OpenedSong(
        val songId: Long,
        val custom: Boolean,
        val timestamp: Long
)