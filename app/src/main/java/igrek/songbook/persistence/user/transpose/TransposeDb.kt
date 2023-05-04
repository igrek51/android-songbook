package igrek.songbook.persistence.user.transpose

import kotlinx.serialization.Serializable

@Serializable
data class TransposeDb(
    val songs: MutableList<TransposedSong> = mutableListOf()
)

@Serializable
data class TransposedSong(
    val songId: String,
    val custom: Boolean,
    var transposition: Int
)