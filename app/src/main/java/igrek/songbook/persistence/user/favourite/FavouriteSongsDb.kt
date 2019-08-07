package igrek.songbook.persistence.user.favourite

import kotlinx.serialization.Serializable

@Serializable
data class FavouriteSongsDb(
        val favourites: MutableList<FavouriteSong>
)

@Serializable
data class FavouriteSong(
        val songId: Long, val custom: Boolean
)