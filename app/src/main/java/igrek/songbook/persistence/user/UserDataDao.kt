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


@Singleton
class UserDataDao {

    @Inject
    lateinit var localDbService: Lazy<LocalDbService>

    var unlockedSongsDao: UnlockedSongsDao? = null
    var favouriteSongsDao: FavouriteSongsDao? = null
    var customSongsDao: CustomSongsDao? = null
    var playlistDao: PlaylistDao? = null
    var openHistoryDao: OpenHistoryDao? = null
    var exclusionDao: ExclusionDao? = null
    var transposeDao: TransposeDao? = null
    var preferencesDao: PreferencesDao? = null

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

    fun read() {
        val path = localDbService.get().songDbDir.absolutePath

        unlockedSongsDao = UnlockedSongsDao(path)
        favouriteSongsDao = FavouriteSongsDao(path)
        customSongsDao = CustomSongsDao(path)
        playlistDao = PlaylistDao(path)
        openHistoryDao = OpenHistoryDao(path)
        exclusionDao = ExclusionDao(path)
        transposeDao = TransposeDao(path)
        preferencesDao = PreferencesDao(path)
    }

    @Synchronized
    fun save() {
        unlockedSongsDao?.save()
        favouriteSongsDao?.save()
        customSongsDao?.save()
        playlistDao?.save()
        openHistoryDao?.save()
        exclusionDao?.save()
        transposeDao?.save()
        preferencesDao?.save()
        logger.info("user data have been saved")
    }

    fun factoryReset() {
        customSongsDao?.factoryReset()
        favouriteSongsDao?.factoryReset()
        unlockedSongsDao?.factoryReset()
        playlistDao?.factoryReset()
        openHistoryDao?.factoryReset()
        exclusionDao?.factoryReset()
        transposeDao?.factoryReset()
        preferencesDao?.factoryReset()
    }

    fun requestSave(toSave: Boolean) {
        saveRequestSubject.onNext(toSave)
    }

    fun saveNow() {
        requestSave(false)
        save()
    }

}