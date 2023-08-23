package igrek.songbook.info.logger

import android.util.Log
import igrek.songbook.BuildConfig
import igrek.songbook.info.errorcheck.formatErrorMessage
import igrek.songbook.inject.appFactory

open class Logger internal constructor() {

    fun error(message: String?) {
        log(message, LogLevel.ERROR, "[ERROR] ")
    }

    fun error(t: Throwable) {
        val throwableMessage = formatErrorMessage(t)
        log(throwableMessage, LogLevel.ERROR, "[ERROR] ")
        printExceptionStackTrace(t)
    }

    fun error(context: String?, t: Throwable) {
        val throwableMessage = formatErrorMessage(t)
        log("$context: $throwableMessage", LogLevel.ERROR, "[ERROR] ")
        printExceptionStackTrace(t)
    }

    open fun fatal(t: Throwable) {
        val exTitle = when {
            t.message.isNullOrEmpty() -> t.javaClass.name
            else -> {
                val throwableMessage = formatErrorMessage(t)
                "${t.javaClass.name} - $throwableMessage"
            }
        }
        printExceptionStackTrace(t)
        log(exTitle, LogLevel.FATAL, "[FATAL] ")
        appFactory.crashlyticsLogger.get().sendCrashlyticsAsync()
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

            val consoleMessage: String =
                if (level.lessOrEqualImportant(LoggerFactory.SHOW_TRACE_DETAILS_LEVEL)) {
                    val externalTrace = getFirstExternalTrace(
                        Thread.currentThread()
                            .stackTrace
                    )

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

            LoggerFactory.sessionLogs.add(
                LogEntry(
                    message = consoleMessage,
                    timestampS = System.currentTimeMillis() / 1000,
                    level = level,
                )
            )

            if (level.moreOrEqualImportant(LogLevel.DEBUG) ||
                (BuildConfig.DEBUG && level.moreOrEqualImportant(LogLevel.DEBUG))
            ) {
                try {
                    appFactory.crashlyticsLogger.get().logCrashlytics(consoleMessage)
                } catch (_: NoSuchMethodError) {
                } catch (_: NoClassDefFoundError) {
                }
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
            synchronized(logMutex) {
                Log.d(tagWithKey(), "[trace] $index - ($fileName:$lineNumber)")
            }
        }
    }

    protected open fun printDebug(msg: String) {
        synchronized(logMutex) {
            Log.d(tagWithKey(), msg)
        }
    }

    protected open fun printInfo(msg: String) {
        synchronized(logMutex) {
            Log.i(tagWithKey(), msg)
        }
    }

    protected open fun printWarn(msg: String) {
        synchronized(logMutex) {
            Log.w(tagWithKey(), msg)
        }
    }

    protected open fun printError(msg: String) {
        synchronized(logMutex) {
            Log.e(tagWithKey(), msg)
        }
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
        private var lastTagKey: Int = 0

        val logMutex = Any()

        /**
         * method synchronized due to multithreading
         * @return next (incremented) tag key number
         */
        private fun incrementTagKey(): Int {
            lastTagKey = (lastTagKey + 1) % 2
            return lastTagKey
        }
    }
}
