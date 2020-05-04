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
        activity.finish()
    }

    fun onStart() {
        logger.debug("starting activity...")
        userDataDao.requestSave(false)
    }

    fun onStop() {
        logger.debug("stopping activity...")
        preferencesService.saveAll()
        userDataDao.requestSave(true)
    }

    fun onDestroy() {
        preferencesService.saveAll()
        userDataDao.saveNow()
        logger.info("activity has been destroyed")
    }

    fun minimize() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(startMain)
    }

}
