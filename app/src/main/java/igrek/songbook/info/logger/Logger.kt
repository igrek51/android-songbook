package igrek.songbook.info.logger

import android.util.Log
import com.google.common.base.Joiner
import com.google.firebase.crashlytics.FirebaseCrashlytics

open class Logger internal constructor() {

    fun error(message: String?) {
        log(message, LogLevel.ERROR, "[ERROR] ")
    }

    fun error(ex: Throwable) {
        log("[${ex.javaClass.name}] ${ex.message}", LogLevel.ERROR, "[ERROR] ")
        printExceptionStackTrace(ex)
    }

    fun error(message: String?, t: Throwable) {
        val msg = "$message: ${t.message}"
        log(msg, LogLevel.ERROR, "[ERROR] ")
    }

    open fun fatal(ex: Throwable) {
        var exTitle = ex.javaClass.name
        if (!ex.message.isNullOrEmpty()) {
            exTitle = "$exTitle - ${ex.message}"
        }
        printExceptionStackTrace(ex)
        log(exTitle, LogLevel.FATAL, "[FATAL] ")
    }

    fun warn(message: String?) {
        log(message, LogLevel.WARN, "[warn]  ")
    }

    fun warn(message: String?, t: Throwable) {
        val msg = "$message: ${t.message}"
        log(msg, LogLevel.WARN, "[warn] ")
    }

    fun info(message: String?) {
        log(message, LogLevel.INFO, "[info]  ")
    }

    fun debug(message: String?) {
        log(message, LogLevel.DEBUG, "[debug] ")
    }

    fun debug(vararg objs: Any?) {
        val message = objs.joinToString(separator = ", ")
        log(message, LogLevel.DEBUG, "[debug] ")
    }

    fun trace(message: String?) {
        log(message, LogLevel.TRACE, "[trace] ")
        printCurrentStackTrace(Thread.currentThread().stackTrace)
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

            if (level.moreOrEqualImportant(LogLevel.ERROR)) {
                logCrashlytics(consoleMessage)
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
        printError(Log.getStackTraceString(ex))
    }

    private fun printCurrentStackTrace(stackTraces: Array<StackTraceElement>) {
        // skip first stack traces: dalvik.system.VMStack, java.lang.Thread
        for (i in 2 until stackTraces.size) {
            val stackTrace = stackTraces[i]
            val fileName = stackTrace.fileName
            val lineNumber = stackTrace.lineNumber
            val index = i - 1
            Log.d(tagWithKey(), "[trace] $index - ($fileName:$lineNumber)")
        }
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

    private fun logCrashlytics(message: String?) {
        val crashlytics = FirebaseCrashlytics.getInstance()
        message?.let { crashlytics.log(it) }
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
