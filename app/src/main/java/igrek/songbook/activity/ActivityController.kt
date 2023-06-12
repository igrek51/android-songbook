package igrek.songbook.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.system.WindowManagerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class ActivityController(
    windowManagerService: LazyInject<WindowManagerService> = appFactory.windowManagerService,
    activity: LazyInject<Activity> = appFactory.activity,
    preferencesService: LazyInject<PreferencesService> = appFactory.preferencesService,
    userDataDao: LazyInject<UserDataDao> = appFactory.userDataDao,
) {
    private val windowManagerService by LazyExtractor(windowManagerService)
    private val activity by LazyExtractor(activity)
    private val preferencesService by LazyExtractor(preferencesService)
    private val userDataDao by LazyExtractor(userDataDao)

    private val logger = LoggerFactory.logger
    var initialized = false
    var isForeground = true
        private set

    fun onConfigurationChanged(newConfig: Configuration) {
        // resize event
        val screenWidthDp = newConfig.screenWidthDp
        val screenHeightDp = newConfig.screenHeightDp
        val orientationName = getOrientationName(newConfig.orientation)
        logger.debug("Screen resized: " + screenWidthDp + "dp x " + screenHeightDp + "dp - " + orientationName)
    }

    private fun getOrientationName(orientation: Int): String {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return "landscape"
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return "portrait"
        }
        return orientation.toString()
    }

    fun quit() {
        windowManagerService.keepScreenOn(false)
        appFactory.crashlyticsLogger.get().sendCrashlytics()
        activity.finish()
    }

    fun quitImmediately() {
        initialized = false
        activity.finish()
    }

    fun onStart() {
        if (!initialized)
            return

        val activityName = activity::class.simpleName
        logger.debug("starting $activityName...")
        userDataDao.requestSave(false)
    }

    fun onStop() {
        if (initialized) {
            val activityName = activity::class.simpleName
            logger.debug("stopping $activityName...")
            preferencesService.dumpAll()
            userDataDao.requestSave(true)
        }
    }

    fun onDestroy() {
        if (initialized) {
            preferencesService.dumpAll()
            runBlocking(Dispatchers.IO) {
                userDataDao.saveNow()
            }
            logger.info("activity destroyed")
        }
    }

    fun onResume() {
        isForeground = true
        appFactory.songCastService.get().refreshSessionIfInRoom()
    }

    fun onPause() {
        isForeground = false
    }

    fun minimize() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(startMain)
    }

    fun isAndroidTv(): Boolean {
        val activityName = activity::class.simpleName
        return activityName == TvActivity::class.simpleName
    }

}
