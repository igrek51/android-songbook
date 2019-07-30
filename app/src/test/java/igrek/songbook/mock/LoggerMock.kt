package igrek.songbook.mock


import android.app.Activity

import igrek.songbook.info.logger.LogLevel
import igrek.songbook.info.logger.Logger

class LoggerMock : Logger() {

    override fun fatal(activity: Activity?, ex: Throwable) {
        log(ex.message, LogLevel.FATAL, "[FATAL ERROR] ")
    }

    override fun printInfo(msg: String) {
        println(msg)
    }

    override fun printError(msg: String) {
        System.err.println(msg)
    }

    override fun printDebug(msg: String) {
        println(msg)
    }

    override fun printWarn(msg: String) {
        println(msg)
    }

}
