package igrek.songbook.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.user.UserDataService
import igrek.songbook.settings.preferences.PreferencesUpdater
import igrek.songbook.system.WindowManagerService
import javax.inject.Inject

class ActivityController {

    @Inject
    lateinit var windowManagerService: WindowManagerService
    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var localDbService: LocalDbService
    @Inject
    lateinit var userDataService: UserDataService
    @Inject
    lateinit var preferencesUpdater: PreferencesUpdater

    private val logger = LoggerFactory.logger

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

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
        localDbService.closeDatabases()
        windowManagerService.keepScreenOn(false)
        activity.finish()
    }

    fun onStart() {}

    fun onStop() {
        userDataService.save()
        preferencesUpdater.updateAndSave()
    }

    fun onDestroy() {
        logger.info("Activity has been destroyed.")
    }

    fun minimize() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(startMain)
    }

}
