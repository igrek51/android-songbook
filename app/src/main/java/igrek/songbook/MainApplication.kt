package igrek.songbook

import android.app.Activity
import android.app.Application
import igrek.songbook.activity.CurrentActivityListener
import igrek.songbook.info.logger.LoggerFactory


class MainApplication : Application() {

    private val logger = LoggerFactory.logger
    private val currentActivityListener = CurrentActivityListener()

    private val currentActivity: Activity
        get() = currentActivityListener.currentActivity!!

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(currentActivityListener)

        // catch all uncaught exceptions
        val defaultUEH = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, th ->
            logger.fatal(currentActivity, th)
            // pass further to OS
            defaultUEH.uncaughtException(thread, th)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterActivityLifecycleCallbacks(currentActivityListener)
    }
}