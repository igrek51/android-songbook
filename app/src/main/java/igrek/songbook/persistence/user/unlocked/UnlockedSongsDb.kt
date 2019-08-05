package igrek.songbook.persistence.user.unlocked
import kotlinx.serialization.*

@Serializable
data class UnlockedSongsDb(val keys: List<String>)
