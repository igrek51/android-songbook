package igrek.songbook.persistence.user.favourite
import kotlinx.serialization.*

@Serializable
data class FavouriteSong(val songId: Long, val custom: Boolean)