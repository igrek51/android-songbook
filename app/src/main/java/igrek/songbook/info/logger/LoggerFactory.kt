package igrek.songbook.info.logger

object LoggerFactory {

    val CONSOLE_LEVEL = LogLevel.TRACE

    val SHOW_TRACE_DETAILS_LEVEL = LogLevel.FATAL

    const val LOG_TAG = "songbook"

    val logger: Logger
        get() = Logger()

    val sessionLogs: MutableList<LogEntry> = mutableListOf()

}
