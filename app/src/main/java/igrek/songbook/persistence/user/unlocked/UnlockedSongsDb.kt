package igrek.songbook.persistence.user.unlocked

import kotlinx.serialization.Serializable

@Serializable
data class UnlockedSongsDb(val keys: MutableList<String>)
