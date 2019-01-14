package igrek.songbook.info.logger

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import com.google.common.base.Joiner

open class Logger internal constructor() {

    fun error(message: String?) {
        log(message, LogLevel.ERROR, "[ERROR] ")
    }

    fun error(ex: Throwable) {
        log(ex.message, LogLevel.ERROR, "[EXCEPTION - " + ex.javaClass.name + "] ")
        printExceptionStackTrace(ex)
    }

    open fun fatal(activity: Activity?, e: String?) {
        log(e, LogLevel.FATAL, "[FATAL] ")
        if (activity == null) {
            error("FATAL ERROR: No activity")
            return
        }
        val dlgAlert = AlertDialog.Builder(activity)
        dlgAlert.setMessage(e)
        dlgAlert.setTitle("Critical error :(")
        dlgAlert.setPositiveButton("Close app") { _, _ -> activity.finish() }
        dlgAlert.setCancelable(false)
        dlgAlert.create().show()
    }

    fun fatal(activity: Activity, ex: Throwable) {
        val e = ex.javaClass.name + " - " + ex.message
        printExceptionStackTrace(ex)
        fatal(activity, e)
    }

    fun warn(message: String?) {
        log(message, LogLevel.WARN, "[warn]  ")
    }

    fun info(message: String?) {
        log(message, LogLevel.INFO, "[info]  ")
    }

    fun debug(message: String?) {
        log(message, LogLevel.DEBUG, "[debug] ")
    }

    fun debug(vararg objs: Any?) {
        val message = Joiner.on(" ").join(objs)
        log(message, LogLevel.DEBUG, "[debug] ")
    }

    fun trace(message: String?) {
        log(message, LogLevel.TRACE, "[trace] ")
    }

    fun trace() {
        log("Quick Trace: " + System.currentTimeMillis(), LogLevel.DEBUG, "[trace] ")
    }

    fun dupa() {
        log("dupa", LogLevel.DEBUG, "[debug] ")
    }

    protected fun log(message: String?, level: LogLevel, logPrefix: String) {
        if (level.moreOrEqualImportant(LoggerFactory.CONSOLE_LEVEL)) {

            val consoleMessage: String
            consoleMessage = if (level.lessOrEqualImportant(LoggerFactory.SHOW_TRACE_DETAILS_LEVEL)) {
                val externalTrace = getFirstExternalTrace(Thread.currentThread()
                        .stackTrace)

                val fileName = externalTrace.fileName
                val lineNumber = externalTrace.lineNumber

                String.format("%s(%s:%d): %s", logPrefix, fileName, lineNumber, message)
            } else {
                logPrefix + message
            }

            when {
                level.moreOrEqualImportant(LogLevel.ERROR) -> printError(consoleMessage)
                level.moreOrEqualImportant(LogLevel.WARN) -> printWarn(consoleMessage)
                level.moreOrEqualImportant(LogLevel.INFO) -> printInfo(consoleMessage)
                else -> printDebug(consoleMessage)
            }
        }
    }

    /**
     * @param stackTraces array of stack traces
     * @return first external stack trace (not in this class)
     */
    private fun getFirstExternalTrace(stackTraces: Array<StackTraceElement>): StackTraceElement {
        var loggerClassFound = false
        // skip first stack traces: dalvik.system.VMStack, java.lang.Thread
        for (i in 2 until stackTraces.size) {
            val stackTrace = stackTraces[i]
            val className = stackTrace.className
            // if it's not from this logger class
            if (className == this.javaClass.name) {
                loggerClassFound = true
            } else if (loggerClassFound) {
                // only when there are previous Logger found
                return stackTrace
            }
        }
        return stackTraces[0]
    }

    private fun printExceptionStackTrace(ex: Throwable) {
        if (LoggerFactory.SHOW_EXCEPTIONS_TRACE)
            printError(Log.getStackTraceString(ex))
    }

    protected open fun printDebug(msg: String) {
        Log.d(tagWithKey(), msg)
    }

    protected open fun printInfo(msg: String) {
        Log.i(tagWithKey(), msg)
    }

    protected open fun printWarn(msg: String) {
        Log.w(tagWithKey(), msg)
    }

    protected open fun printError(msg: String) {
        Log.e(tagWithKey(), msg)
    }

    /**
     * force logcat to align all logs the same way
     * by generating different tags for every next log.
     * (to prevent log header cutting by logcat)
     * https://github.com/orhanobut/logger/issues/173
     */
    private fun tagWithKey(): String {
        return LoggerFactory.LOG_TAG + incrementTagKey()
    }

    companion object {

        private var lastTagKey = 0

        /**
         * method synchronized due to multithreading
         * @return next (incremented) tag key number
         */
        @Synchronized
        private fun incrementTagKey(): Int {
            lastTagKey = (lastTagKey + 1) % 5
            return lastTagKey
        }

        fun debug(vararg objs: Any) {
            Logger().debug(*objs)
        }
    }
}