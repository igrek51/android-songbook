package igrek.songbook.info.logger

enum class LogLevel constructor(private val levelNumber: Int) {

    FATAL(10),

    ERROR(20),

    WARN(30),

    INFO(40),

    DEBUG(50),

    TRACE(60),

    fun moreOrEqualImportant(than: LogLevel): Boolean {
        return levelNumber <= than.levelNumber
    }

    fun lessOrEqualImportant(than: LogLevel): Boolean {
        return levelNumber >= than.levelNumber
    }

}
