package igrek.songbook

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import igrek.songbook.activity.CurrentActivityListener
import igrek.songbook.info.logger.LoggerFactory


class MainApplication : Application() {

    private val logger = LoggerFactory.logger
    private val currentActivityListener = CurrentActivityListener()

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(currentActivityListener)

        // catch all uncaught exceptions
        val defaultUEH = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, th ->
            logger.fatal(th)
            // pass further to OS
            defaultUEH.uncaughtException(thread, th)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterActivityLifecycleCallbacks(currentActivityListener)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}