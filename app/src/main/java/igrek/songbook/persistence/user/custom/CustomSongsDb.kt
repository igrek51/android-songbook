package igrek.songbook.persistence.user.custom

import kotlinx.serialization.Serializable


@Serializable
data class CustomSongsDb(var songs: MutableList<CustomSong>)