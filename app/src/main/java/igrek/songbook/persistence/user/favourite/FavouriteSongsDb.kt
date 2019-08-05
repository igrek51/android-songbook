package igrek.songbook.persistence.user.favourite

import kotlinx.serialization.Serializable

@Serializable
data class FavouriteSongsDb(val favourites: MutableList<FavouriteSong>)