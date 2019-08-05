package igrek.songbook.persistence.user.custom
import kotlinx.serialization.*


@Serializable
data class CustomSongsDb(val songs: List<CustomSong>)