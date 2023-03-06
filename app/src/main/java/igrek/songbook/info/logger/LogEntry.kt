package igrek.songbook.info.logger

data class LogEntry(
    val message: String,
    val timestampS: Long,
    val level: LogLevel,
)
