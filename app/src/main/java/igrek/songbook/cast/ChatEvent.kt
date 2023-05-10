package igrek.songbook.cast

import igrek.songbook.persistence.general.model.Song

open class ChatEvent(
    open val timestamp: Long,
)

data class SystemChatEvent(
    override val timestamp: Long,
    val text: String,
) : ChatEvent(timestamp)

data class MessageChatEvent(
    override val timestamp: Long,
    val author: String,
    val text: String,
) : ChatEvent(timestamp)

data class SongChatEvent(
    override val timestamp: Long,
    val author: String,
    val song: Song,
) : ChatEvent(timestamp)
