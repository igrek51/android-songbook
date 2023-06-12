package igrek.songbook.persistence.user

import android.annotation.SuppressLint
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.ContextError
import igrek.songbook.info.errorcheck.RetryAttempt
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.LocalFilesystem
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.custom.CustomSongsDao
import igrek.songbook.persistence.user.exclusion.ExclusionDao
import igrek.songbook.persistence.user.favourite.FavouriteSongsDao
import igrek.songbook.persistence.user.history.OpenHistoryDao
import igrek.songbook.persistence.user.playlist.PlaylistDao
import igrek.songbook.persistence.user.preferences.PreferencesDao
import igrek.songbook.persistence.user.songtweak.SongTweakDao
import igrek.songbook.persistence.user.transpose.TransposeDao
import igrek.songbook.persistence.user.unlocked.UnlockedSongsDao
import igrek.songbook.util.ioScope
import igrek.songbook.util.launchAndJoin
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@SuppressLint("CheckResult")
class UserDataDao(
    localFilesystem: LazyInject<LocalFilesystem> = appFactory.localFilesystem,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
) {
    internal val localDbService by LazyExtractor(localFilesystem)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val activityController by LazyExtractor(activityController)
    private val songsRepository by LazyExtractor(songsRepository)

    var unlockedSongsDao: UnlockedSongsDao by LazyDaoLoader { path -> UnlockedSongsDao(path) }
    var favouriteSongsDao: FavouriteSongsDao by LazyDaoLoader { path -> FavouriteSongsDao(path) }
    var customSongsDao: CustomSongsDao by LazyDaoLoader { path -> CustomSongsDao(path) }
    var playlistDao: PlaylistDao by LazyDaoLoader { path -> PlaylistDao(path) }
    var openHistoryDao: OpenHistoryDao by LazyDaoLoader { path -> OpenHistoryDao(path) }
    var exclusionDao: ExclusionDao by LazyDaoLoader { path -> ExclusionDao(path) }
    var transposeDao: TransposeDao by LazyDaoLoader { path -> TransposeDao(path) }
    var preferencesDao: PreferencesDao by LazyDaoLoader { path -> PreferencesDao(path) }
    var songTweakDao: SongTweakDao by LazyDaoLoader { path -> SongTweakDao(path) }

    private var saveRequestSubject: PublishSubject<Boolean> = PublishSubject.create()
    private val logger = LoggerFactory.logger
    private val dataTransferMutex = Mutex()

    init {
        saveRequestSubject
            .throttleLast(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ toSave ->
                if (toSave) {
                    ioScope.launch {
                        save()
                    }
                }
            }, UiErrorHandler::handleError)
    }

    suspend fun load() {
        runCatching {
            RetryAttempt(3, "loading user data", backoffDelayMs = 200).run {
                reload(resetOnError = false)
            }
        }.recoverCatching {
            logger.error("failed to load user data. Trying synchronous load...")
            appFactory.crashlyticsLogger.get().reportNonFatalError(it)
            reloadSync()
        }.recoverCatching {
            delay(1000)
            reloadSync()
        }.recover { t ->
            logger.error("failed to load user data", t)
            throw ContextError("Corrupted user data", t)
        }
    }

    suspend fun loadOrExit(): Boolean {
        try {
            load()
            return true
        } catch (t: Throwable) {
            appFactory.crashlyticsLogger.get().reportNonFatalError(t)
            val dialogMessage = uiInfoService.resString(R.string.error_corrupted_user_data_message, t.message.orEmpty())

            uiInfoService.dialogThreeChoices(
                titleResId = R.string.error_corrupted_user_data,
                message = dialogMessage,
                positiveButton = R.string.action_exit,
                positiveAction = {
                    logger.debug("quitting now...", t)
                    activityController.quitImmediately()
                },
                negativeButton = R.string.action_reset_corrupted_data,
                negativeAction = {
                    GlobalScope.launch(Dispatchers.IO) {
                        resetCorruptedUserData()
                    }
                },
                cancelable = false,
            )
            return false
        }
    }

    private suspend fun reload(resetOnError: Boolean) {
        val path = localDbService.appFilesDir.absolutePath

        dataTransferMutex.withLock {
            launchAndJoin(
                { unlockedSongsDao = UnlockedSongsDao(path, resetOnError=resetOnError) },
                { favouriteSongsDao = FavouriteSongsDao(path, resetOnError=resetOnError) },
                { customSongsDao = CustomSongsDao(path, resetOnError=resetOnError) },
                { playlistDao = PlaylistDao(path, resetOnError=resetOnError) },
                { openHistoryDao = OpenHistoryDao(path, resetOnError=resetOnError) },
                { exclusionDao = ExclusionDao(path, resetOnError=resetOnError) },
                { transposeDao = TransposeDao(path, resetOnError=resetOnError) },
                { preferencesDao = PreferencesDao(path, resetOnError=resetOnError) },
                { songTweakDao = SongTweakDao(path, resetOnError=resetOnError) },
            )
        }

        logger.debug("User data loaded")
    }

    private suspend fun reloadSync() {
        val resetOnError = false
        val path = localDbService.appFilesDir.absolutePath
        dataTransferMutex.withLock {
            logger.debug("Sync-loading data from $path")
            withContext(Dispatchers.Main) {
                unlockedSongsDao = UnlockedSongsDao(path, resetOnError = resetOnError)
                favouriteSongsDao = FavouriteSongsDao(path, resetOnError = resetOnError)
                customSongsDao = CustomSongsDao(path, resetOnError = resetOnError)
                playlistDao = PlaylistDao(path, resetOnError = resetOnError)
                openHistoryDao = OpenHistoryDao(path, resetOnError = resetOnError)
                exclusionDao = ExclusionDao(path, resetOnError = resetOnError)
                transposeDao = TransposeDao(path, resetOnError = resetOnError)
                preferencesDao = PreferencesDao(path, resetOnError = resetOnError)
                songTweakDao = SongTweakDao(path, resetOnError = resetOnError)
            }
        }
        logger.debug("User data loaded synchronously")
    }

    fun reloadCustomSongs() {
        val path = localDbService.appFilesDir.absolutePath
        runBlocking(Dispatchers.IO) {
            dataTransferMutex.withLock {
                customSongsDao = CustomSongsDao(path, resetOnError=false)
            }
            songsRepository.reloadCustomSongsDb()
        }
        logger.debug("Custom songs data loaded")
    }

    suspend fun save() {
        dataTransferMutex.withLock {
            launchAndJoin(
                { unlockedSongsDao.save() },
                { favouriteSongsDao.save() },
                { customSongsDao.save() },
                { playlistDao.save() },
                { openHistoryDao.save() },
                { exclusionDao.save() },
                { transposeDao.save() },
                { preferencesDao.save() },
                { songTweakDao.save() },
            )
        }
        logger.info("User data saved")
    }

    private suspend fun resetCorruptedUserData() {
        logger.warn("Resetting corrupted user data")
        reload(resetOnError=true)
        saveNow()
        uiInfoService.showToast(R.string.restart_needed)
        withContext(Dispatchers.Main) {
            activityController.quit()
        }
    }

    suspend fun factoryReset() {
        logger.warn("Factory reset: User data...")
        dataTransferMutex.withLock {
            customSongsDao.factoryReset()
            favouriteSongsDao.factoryReset()
            unlockedSongsDao.factoryReset()
            playlistDao.factoryReset()
            openHistoryDao.factoryReset()
            exclusionDao.factoryReset()
            transposeDao.factoryReset()
            preferencesDao.factoryReset()
            songTweakDao.factoryReset()
        }
    }

    fun requestSave(toSave: Boolean) {
        saveRequestSubject.onNext(toSave)
    }

    suspend fun saveNow() {
        requestSave(false)
        save()
    }

}

class LazyDaoLoader<T : AbstractJsonDao<out Any>>(
    private val loader: (path: String) -> T,
) : ReadWriteProperty<UserDataDao, T> {

    private var loaded: T? = null

    override fun getValue(thisRef: UserDataDao, property: KProperty<*>): T {
        val loadedVal = loaded
        if (loadedVal != null)
            return loadedVal

        val path = thisRef.localDbService.appFilesDir.absolutePath
        val loadedNN = loader.invoke(path)
        loaded = loadedNN
        logger.debug("User data lazy-loaded: $path")
        return loadedNN
    }

    override fun setValue(thisRef: UserDataDao, property: KProperty<*>, value: T) {
        loaded = value
    }
}
