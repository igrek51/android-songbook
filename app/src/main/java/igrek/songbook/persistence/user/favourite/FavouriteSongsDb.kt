package igrek.songbook.persistence.user.favourite
import kotlinx.serialization.*

@Serializable
data class FavouriteSongsDb(val favourites: List<FavouriteSong>)