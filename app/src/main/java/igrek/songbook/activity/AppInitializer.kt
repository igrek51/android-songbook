package igrek.songbook.activity


import android.app.Activity
import igrek.songbook.BuildConfig
import igrek.songbook.admin.AdminService
import igrek.songbook.custom.ShareSongService
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    activity: LazyInject<Activity> = appFactory.activity,
    shareSongService: LazyInject<ShareSongService> = appFactory.shareSongService,
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
    private val activityController by LazyExtractor(activityController)
    private val activity by LazyExtractor(activity)
    private val shareSongService by LazyExtractor(shareSongService)

    private val logger = LoggerFactory.logger
    private val startingScreen: KClass<out MainLayout> = TopSongsLayoutController::class
    private val debugInitEnabled = false

    fun init() {
        if (debugInitEnabled && BuildConfig.DEBUG) {
            debugInit()
        }

        logger.info("Initializing application...")

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                appLanguageService.setLocale()
                songsRepository.init()
                layoutController.init()
                windowManagerService.hideTaskbar()

                layoutController.showLayout(startingScreen).join()

                songsUpdater.checkUpdateIsAvailable()

                adService.initialize()
                appLanguageService.setLocale() // fix locale after admob init

                adminService.init()
                if (isRunningFirstTime())
                    firstRunInit()
                reportExecution()

                activityController.initialized = true
                postInit()
            }

            logger.info("Application has been initialized.")
        }
    }

    fun postInit() {
        activity.intent.getStringExtra("encodedSong")?.let { encodedSong ->
            shareSongService.openSharedEncodedSong(encodedSong)
        }
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
