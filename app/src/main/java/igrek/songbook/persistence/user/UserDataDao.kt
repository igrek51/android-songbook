package igrek.songbook.persistence.user

import android.annotation.SuppressLint
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.RetryAttempt
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.user.custom.CustomSongsDao
import igrek.songbook.persistence.user.exclusion.ExclusionDao
import igrek.songbook.persistence.user.favourite.FavouriteSongsDao
import igrek.songbook.persistence.user.history.OpenHistoryDao
import igrek.songbook.persistence.user.playlist.PlaylistDao
import igrek.songbook.persistence.user.preferences.PreferencesDao
import igrek.songbook.persistence.user.songtweak.SongTweakDao
import igrek.songbook.persistence.user.transpose.TransposeDao
import igrek.songbook.persistence.user.unlocked.UnlockedSongsDao
import igrek.songbook.util.launchAndJoin
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@OptIn(DelicateCoroutinesApi::class)
@SuppressLint("CheckResult")
class UserDataDao(
    localDbService: LazyInject<LocalDbService> = appFactory.localDbService,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
) {
    internal val localDbService by LazyExtractor(localDbService)
    private val uiInfoService by LazyExtractor(uiInfoService)

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
            .throttleLast(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ toSave ->
                if (toSave) {
                    GlobalScope.launch(Dispatchers.IO) {
                        save()
                    }
                }
            }, UiErrorHandler::handleError)
    }

    suspend fun load() {
        try {
            RetryAttempt(3, "load user data").run {
                reload()
            }
        } catch (t: Throwable) {
            logger.error("failed to load user data", t)
            uiInfoService.showToast("Can't load corrupted user data")
            throw RuntimeException("Can't load corrupted user data", t)
        }
    }

    suspend fun reload() {
        val path = localDbService.appFilesDir.absolutePath

        dataTransferMutex.withLock {
            launchAndJoin(
                { unlockedSongsDao = UnlockedSongsDao(path) },
                { favouriteSongsDao = FavouriteSongsDao(path) },
                { customSongsDao = CustomSongsDao(path) },
                { playlistDao = PlaylistDao(path) },
                { openHistoryDao = OpenHistoryDao(path) },
                { exclusionDao = ExclusionDao(path) },
                { transposeDao = TransposeDao(path) },
                { preferencesDao = PreferencesDao(path) },
                { songTweakDao = SongTweakDao(path) },
            )
        }

        logger.debug("User data loaded")
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
