package igrek.songbook.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.system.WindowManagerService
import javax.inject.Inject

class ActivityController {

    @Inject
    lateinit var windowManagerService: Lazy<WindowManagerService>
    @Inject
    lateinit var activity: Lazy<Activity>
    @Inject
    lateinit var songsRepository: Lazy<SongsRepository>
    @Inject
    lateinit var preferencesState: Lazy<PreferencesState>

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
        windowManagerService.get().keepScreenOn(false)
        activity.get().finish()
    }

    fun onStart() {
        logger.debug("starting activity...")
        songsRepository.get().requestSave(false)
    }

    fun onStop() {
        logger.debug("stopping activity...")
        songsRepository.get().requestSave(true)
        preferencesState.get().updateAndSave()
    }

    fun onDestroy() {
        songsRepository.get().saveNow()
        logger.info("activity has been destroyed")
    }

    fun minimize() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.get().startActivity(startMain)
    }

}
