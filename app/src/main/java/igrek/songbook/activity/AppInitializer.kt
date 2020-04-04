package igrek.songbook.activity

import android.app.Activity
import dagger.Lazy
import igrek.songbook.BuildConfig
import igrek.songbook.admin.AdminService
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.ad.AdService
import igrek.songbook.persistence.general.SongsUpdater
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.PreferencesState
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
    @Inject
    lateinit var preferencesState: Lazy<PreferencesState>
    @Inject
    lateinit var userDataDao: Lazy<UserDataDao>
    @Inject
    lateinit var chordsNotationService: Lazy<ChordsNotationService>
    @Inject
    lateinit var adService: Lazy<AdService>

    private val logger = LoggerFactory.logger

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun init() {
        if (BuildConfig.DEBUG) {
            debugInit()
        }

        logger.info("Initializing application...")

        appLanguageService.get().setLocale()
        songsRepository.get().init()
        layoutController.get().init()
        windowManagerService.get().hideTaskbar()
        layoutController.get().showLayout(SongTreeLayoutController::class)
        songsUpdater.get().checkUpdateIsAvailable()
        adService.get().initialize()
        adminService.get().init()
        if (isRunningFirstTime())
            firstRunInit()
        reportExecution()

        logger.info("Application has been initialized.")
    }

    private fun firstRunInit() {
        chordsNotationService.get().setDefaultChordsNotation()
    }

    private fun debugInit() {
        // Allow showing the activity even if the device is locked
        windowManagerService.get().showAppWhenLocked()
    }

    private fun isRunningFirstTime(): Boolean {
        return preferencesState.get().appExecutionCount == 0L
    }

    private fun reportExecution() {
        preferencesState.get().appExecutionCount += 1
    }

}
