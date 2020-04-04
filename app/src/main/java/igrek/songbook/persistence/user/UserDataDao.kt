package igrek.songbook.persistence.user

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.user.custom.CustomSongsDao
import igrek.songbook.persistence.user.exclusion.ExclusionDao
import igrek.songbook.persistence.user.favourite.FavouriteSongsDao
import igrek.songbook.persistence.user.history.OpenHistoryDao
import igrek.songbook.persistence.user.playlist.PlaylistDao
import igrek.songbook.persistence.user.preferences.PreferencesDao
import igrek.songbook.persistence.user.transpose.TransposeDao
import igrek.songbook.persistence.user.unlocked.UnlockedSongsDao
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


@Singleton
class UserDataDao {

    @Inject
    lateinit var localDbService: Lazy<LocalDbService>

    var unlockedSongsDao: UnlockedSongsDao by LazyDaoLoader { path -> UnlockedSongsDao(path) }
    var favouriteSongsDao: FavouriteSongsDao by LazyDaoLoader { path -> FavouriteSongsDao(path) }
    var customSongsDao: CustomSongsDao by LazyDaoLoader { path -> CustomSongsDao(path) }
    var playlistDao: PlaylistDao by LazyDaoLoader { path -> PlaylistDao(path) }
    var openHistoryDao: OpenHistoryDao by LazyDaoLoader { path -> OpenHistoryDao(path) }
    var exclusionDao: ExclusionDao by LazyDaoLoader { path -> ExclusionDao(path) }
    var transposeDao: TransposeDao by LazyDaoLoader { path -> TransposeDao(path) }
    var preferencesDao: PreferencesDao by LazyDaoLoader { path -> PreferencesDao(path) }

    private var saveRequestSubject: PublishSubject<Boolean> = PublishSubject.create()
    private val logger = LoggerFactory.logger

    init {
        DaggerIoc.factoryComponent.inject(this)

        saveRequestSubject
                .debounce(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { toSave ->
                    if (toSave)
                        save()
                }
    }

    fun reload() {
        val path = localDbService.get().songDbDir.absolutePath

        unlockedSongsDao = UnlockedSongsDao(path)
        favouriteSongsDao = FavouriteSongsDao(path)
        customSongsDao = CustomSongsDao(path)
        playlistDao = PlaylistDao(path)
        openHistoryDao = OpenHistoryDao(path)
        exclusionDao = ExclusionDao(path)
        transposeDao = TransposeDao(path)
        preferencesDao = PreferencesDao(path)

        logger.trace("user data reloaded")
    }

    @Synchronized
    fun save() {
        unlockedSongsDao.save()
        favouriteSongsDao.save()
        customSongsDao.save()
        playlistDao.save()
        openHistoryDao.save()
        exclusionDao.save()
        transposeDao.save()
        preferencesDao.save()
        logger.info("user data saved")
    }

    fun factoryReset() {
        customSongsDao.factoryReset()
        favouriteSongsDao.factoryReset()
        unlockedSongsDao.factoryReset()
        playlistDao.factoryReset()
        openHistoryDao.factoryReset()
        exclusionDao.factoryReset()
        transposeDao.factoryReset()
        preferencesDao.factoryReset()
    }

    fun requestSave(toSave: Boolean) {
        saveRequestSubject.onNext(toSave)
    }

    fun saveNow() {
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

        val path = thisRef.localDbService.get().songDbDir.absolutePath
        val loadedNN = loader.invoke(path)
        loaded = loadedNN
        return loadedNN
    }

    override fun setValue(thisRef: UserDataDao, property: KProperty<*>, value: T) {
        loaded = value
    }
}
