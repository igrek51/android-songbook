package igrek.songbook.cast

import igrek.songbook.persistence.general.model.Song

open class LogEvent(
    open val timestamp: Long,
)

data class SystemLogEvent(
    override val timestamp: Long,
    val text: String,
) : LogEvent(timestamp)

data class MessageLogEvent(
    override val timestamp: Long,
    val author: String,
    val text: String,
) : LogEvent(timestamp)

data class SongLogEvent(
    override val timestamp: Long,
    val author: String,
    val song: Song,
) : LogEvent(timestamp)
