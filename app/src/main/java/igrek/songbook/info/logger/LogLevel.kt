package igrek.songbook.info.logger

enum class LogLevel constructor(private val levelNumber: Int) {

    /** lower number - higher priority (more important)  */

    OFF(0), // for settings only

    FATAL(10),

    ERROR(20),

    WARN(30),

    INFO(40),

    DEBUG(50),

    TRACE(60),

    ALL(1000);

    fun moreOrEqualImportant(than: LogLevel): Boolean {
        return levelNumber <= than.levelNumber
    }

    fun lessOrEqualImportant(than: LogLevel): Boolean {
        return levelNumber >= than.levelNumber
    }

}
