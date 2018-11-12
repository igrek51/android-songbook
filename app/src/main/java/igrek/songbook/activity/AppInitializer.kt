package igrek.songbook.activity

import android.app.Activity
import igrek.songbook.BuildConfig
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.SongsUpdater
import igrek.songbook.system.WindowManagerService
import java.util.*
import javax.inject.Inject

class AppInitializer {

    @Inject
    lateinit var windowManagerService: WindowManagerService
    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var layoutController: LayoutController
    @Inject
    lateinit var songsUpdater: SongsUpdater

    private val logger = LoggerFactory.getLogger()

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun init() {
        if (BuildConfig.DEBUG) {
            debugInit()
        }

        windowManagerService.hideTaskbar()
        layoutController.init()
        layoutController.showSongTree()
        songsUpdater.checkUpdateIsAvailable()

        logger.info("Application has been initialized.")
    }

    private fun debugInit() {
        // Allow showing the activity even if the device is locked
        windowManagerService.showAppWhenLocked()
        // setLocale("pl");
    }

    /**
     * forces locale settings
     * @param langCode language code (pl)
     */
    private fun setLocale(langCode: String) {
        val res = activity.resources
        // Change locale settings in the app.
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = Locale(langCode.toLowerCase())
        res.updateConfiguration(conf, dm)
    }

}
