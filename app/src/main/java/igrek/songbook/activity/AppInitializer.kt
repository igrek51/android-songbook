package igrek.songbook.activity

import android.app.Activity
import dagger.Lazy
import igrek.songbook.BuildConfig
import igrek.songbook.admin.AdminService
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.persistence.general.SongsUpdater
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.songselection.tree.SongTreeLayoutController
import igrek.songbook.system.WindowManagerService
import javax.inject.Inject

class AppInitializer {

    @Inject
    lateinit var windowManagerService: Lazy<WindowManagerService>
    @Inject
    lateinit var activity: Lazy<Activity>
    @Inject
    lateinit var layoutController: Lazy<LayoutController>
    @Inject
    lateinit var songsUpdater: Lazy<SongsUpdater>
    @Inject
    lateinit var appLanguageService: Lazy<AppLanguageService>
    @Inject
    lateinit var songsRepository: Lazy<SongsRepository>
    @Inject
    lateinit var adminService: Lazy<AdminService>

    private val logger = LoggerFactory.logger

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun init() {
        if (BuildConfig.DEBUG) {
            debugInit()
        }

        appLanguageService.get().setLocale()
        windowManagerService.get().hideTaskbar()
        songsRepository.get().init()
        layoutController.get().init()
        layoutController.get().showLayout(SongTreeLayoutController::class)
        songsUpdater.get().checkUpdateIsAvailable()
        adminService.get().init()

        logger.info("Application has been initialized.")
    }

    private fun debugInit() {
        // Allow showing the activity even if the device is locked
        windowManagerService.get().showAppWhenLocked()
    }

}
