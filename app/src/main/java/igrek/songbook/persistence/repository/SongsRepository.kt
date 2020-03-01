package igrek.songbook.persistence.repository

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.general.dao.PublicSongsDao
import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.CategoryType
import igrek.songbook.persistence.repository.builder.CustomSongsDbBuilder
import igrek.songbook.persistence.repository.builder.PublicSongsDbBuilder
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.persistence.user.custom.CustomSongsDao
import igrek.songbook.persistence.user.exclusion.ExclusionDao
import igrek.songbook.persistence.user.favourite.FavouriteSongsDao
import igrek.songbook.persistence.user.history.OpenHistoryDao
import igrek.songbook.persistence.user.playlist.PlaylistDao
import igrek.songbook.persistence.user.transpose.TransposeDao
import igrek.songbook.persistence.user.unlocked.UnlockedSongsDao
import igrek.songbook.util.lookup.SimpleCache
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongsRepository {

    @Inject
    lateinit var localDbService: Lazy<LocalDbService>
    @Inject
    lateinit var userDataDao: Lazy<UserDataDao>
    @Inject
    lateinit var uiResourceService: Lazy<UiResourceService>

    private val logger = LoggerFactory.logger

    var dbChangeSubject: PublishSubject<Boolean> = PublishSubject.create()
    private var saveRequestSubject: PublishSubject<Boolean> = PublishSubject.create()

    var publicSongsRepo: PublicSongsRepository = PublicSongsRepository(0, SimpleCache.emptyList(), SimpleCache.emptyList())
    var customSongsRepo: CustomSongsRepository = CustomSongsRepository(
            SimpleCache.emptyList(),
            SimpleCache.emptyList(),
            allCustomCategory = Category(
                    id = CategoryType.CUSTOM.id,
                    type = CategoryType.CUSTOM,
                    name = null,
                    custom = false,
                    songs = mutableListOf()
            )
    )
    var allSongsRepo: AllSongsRepository = AllSongsRepository(publicSongsRepo, customSongsRepo)
    private var publicSongsDao: PublicSongsDao? = null

    val unlockedSongsDao: UnlockedSongsDao get() = userDataDao.get().unlockedSongsDao!!
    val favouriteSongsDao: FavouriteSongsDao get() = userDataDao.get().favouriteSongsDao!!
    val customSongsDao: CustomSongsDao get() = userDataDao.get().customSongsDao!!
    val playlistDao: PlaylistDao get() = userDataDao.get().playlistDao!!
    val openHistoryDao: OpenHistoryDao get() = userDataDao.get().openHistoryDao!!
    val exclusionDao: ExclusionDao get() = userDataDao.get().exclusionDao!!
    val transposeDao: TransposeDao get() = userDataDao.get().transposeDao!!

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

    fun init() {
        try {
            reloadSongsDb()
        } catch (t: Throwable) {
            factoryReset()
        }
    }

    @Synchronized
    private fun save() {
        userDataDao.get().save()
    }

    fun requestSave(toSave: Boolean) {
        saveRequestSubject.onNext(toSave)
    }

    fun saveNow() {
        requestSave(false)
        save()
    }

    @Synchronized
    fun close() {
        publicSongsDao?.close()
        userDataDao.get().save()
    }

    @Synchronized
    fun reloadSongsDb() {
        val versionNumber = try {
            loadPublicData()
        } catch (t: Throwable) {
            logger.error("failed to load general data", t)
            resetGeneralData()
            loadPublicData()
        }

        try {
            userDataDao.get().read()
        } catch (t: Throwable) {
            logger.error("failed to load user data", t)
            resetUserData()
            userDataDao.get().read()
        }

        val publicDbBuilder = PublicSongsDbBuilder(versionNumber, publicSongsDao!!, userDataDao.get())
        val customDbBuilder = CustomSongsDbBuilder(userDataDao.get())

        publicSongsRepo = publicDbBuilder.buildPublic(uiResourceService.get())
        customSongsRepo = customDbBuilder.buildCustom()
        allSongsRepo = AllSongsRepository(publicSongsRepo, customSongsRepo)

        dbChangeSubject.onNext(true)
    }

    @Synchronized
    fun reloadCustomSongsDb() {
        try {
            userDataDao.get().read()
        } catch (t: Throwable) {
            logger.error("failed to load user data", t)
            resetUserData()
            userDataDao.get().read()
        }

        val customDbBuilder = CustomSongsDbBuilder(userDataDao.get())
        customSongsRepo = customDbBuilder.buildCustom()
        allSongsRepo.invalidate()
        dbChangeSubject.onNext(true)
    }

    private fun loadPublicData(): Long {
        localDbService.get().ensureLocalDbExists()
        val dbFile = localDbService.get().songsDbFile
        publicSongsDao = PublicSongsDao(dbFile)
        val versionNumber = publicSongsDao?.readDbVersionNumber()
                ?: throw RuntimeException("invalid local songs database format")
        publicSongsDao!!.verifyDbVersion(versionNumber)
        return versionNumber
    }

    @Synchronized
    fun factoryReset() {
        logger.warn("Songs database factory reset...")
        resetGeneralData()
        resetUserData()
        reloadSongsDb()
    }

    fun resetGeneralData() {
        logger.warn("resetting general data...")
        publicSongsDao?.close()
        localDbService.get().factoryReset()
    }

    fun resetUserData() {
        logger.warn("resetting user data...")
        userDataDao.get().factoryReset()
    }

    @Synchronized
    fun songsDbVersion(): Long? {
        return publicSongsDao?.readDbVersionNumber()
    }

    fun reloadUserData() {
        userDataDao.get().save()
        reloadCustomSongsDb()
    }

}
