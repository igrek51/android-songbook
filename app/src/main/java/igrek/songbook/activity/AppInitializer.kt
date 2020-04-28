package igrek.songbook.activity


import igrek.songbook.BuildConfig
import igrek.songbook.admin.AdminService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.LayoutController
import igrek.songbook.layout.MainLayout
import igrek.songbook.layout.ad.AdService
import igrek.songbook.persistence.general.SongsUpdater
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.chordsnotation.ChordsNotationService
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.songselection.top.TopSongsLayoutController
import igrek.songbook.system.WindowManagerService
import kotlin.reflect.KClass

class AppInitializer(
        windowManagerService: LazyInject<WindowManagerService> = appFactory.windowManagerService,
        layoutController: LazyInject<LayoutController> = appFactory.layoutController,
        songsUpdater: LazyInject<SongsUpdater> = appFactory.songsUpdater,
        appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
        adminService: LazyInject<AdminService> = appFactory.adminService,
        preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
        chordsNotationService: LazyInject<ChordsNotationService> = appFactory.chordsNotationService,
        adService: LazyInject<AdService> = appFactory.adService,
) {
    private val windowManagerService by LazyExtractor(windowManagerService)
    private val layoutController by LazyExtractor(layoutController)
    private val songsUpdater by LazyExtractor(songsUpdater)
    private val appLanguageService by LazyExtractor(appLanguageService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val adminService by LazyExtractor(adminService)
    private val preferencesState by LazyExtractor(preferencesState)
    private val chordsNotationService by LazyExtractor(chordsNotationService)
    private val adService by LazyExtractor(adService)

    private val logger = LoggerFactory.logger
    private val startingScreen: KClass<out MainLayout> = TopSongsLayoutController::class

    fun init() {
        if (BuildConfig.DEBUG) {
            debugInit()
        }

        logger.info("Initializing application...")

        appLanguageService.setLocale()
        songsRepository.init()
        layoutController.init()
        windowManagerService.hideTaskbar()
        layoutController.showLayout(startingScreen)
        songsUpdater.checkUpdateIsAvailable()
        adService.initialize()
        adminService.init()
        if (isRunningFirstTime())
            firstRunInit()
        reportExecution()

        logger.info("Application has been initialized.")
    }

    private fun firstRunInit() {
        chordsNotationService.setDefaultChordsNotation()
    }

    private fun debugInit() {
        // Allow showing the activity even if the device is locked
        windowManagerService.showAppWhenLocked()
    }

    private fun isRunningFirstTime(): Boolean {
        return preferencesState.appExecutionCount == 0L
    }

    private fun reportExecution() {
        preferencesState.appExecutionCount += 1
    }

}
