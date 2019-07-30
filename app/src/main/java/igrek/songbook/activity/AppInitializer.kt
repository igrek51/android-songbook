package igrek.songbook.activity

import android.app.Activity
import igrek.songbook.BuildConfig
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.SongsUpdater
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.system.WindowManagerService
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
    @Inject
    lateinit var appLanguageService: AppLanguageService

    private val logger = LoggerFactory.logger

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun init() {
        if (BuildConfig.DEBUG) {
            debugInit()
        }

        appLanguageService.setLocale()
        windowManagerService.hideTaskbar()
        layoutController.init()
        layoutController.showSongTree()
        songsUpdater.checkUpdateIsAvailable()

        logger.info("Application has been initialized.")
    }

    private fun debugInit() {
        // Allow showing the activity even if the device is locked
        windowManagerService.showAppWhenLocked()
    }

}
