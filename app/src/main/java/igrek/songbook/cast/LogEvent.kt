package igrek.songbook.cast

import igrek.songbook.persistence.general.model.Song

open class LogEvent(
    open val timestampMs: Long, // in milliseconds
)

data class SystemLogEvent(
    override val timestampMs: Long,
    val text: String,
) : LogEvent(timestampMs)

data class MessageLogEvent(
    override val timestampMs: Long,
    val author: String,
    val text: String,
) : LogEvent(timestampMs)

data class SongLogEvent(
    override val timestampMs: Long,
    val author: String,
    val song: Song,
) : LogEvent(timestampMs)
