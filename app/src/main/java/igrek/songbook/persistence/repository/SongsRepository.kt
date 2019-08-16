package igrek.songbook.persistence.repository

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.general.SongsUpdater
import igrek.songbook.persistence.general.dao.GeneralSongsDao
import igrek.songbook.persistence.general.model.SongsDb
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.persistence.user.custom.CustomSongsDao
import igrek.songbook.persistence.user.exclusion.ExclusionDao
import igrek.songbook.persistence.user.favourite.FavouriteSongsDao
import igrek.songbook.persistence.user.playlist.OpenHistoryDao
import igrek.songbook.persistence.user.playlist.PlaylistDao
import igrek.songbook.persistence.user.unlocked.UnlockedSongsDao
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SongsRepository {

    @Inject
    lateinit var localDbService: Lazy<LocalDbService>
    @Inject
    lateinit var userDataDao: Lazy<UserDataDao>
    @Inject
    lateinit var uiResourceService: Lazy<UiResourceService>
    @Inject
    lateinit var uiInfoService: Lazy<UiInfoService>
    @Inject
    lateinit var songsUpdater: Lazy<SongsUpdater>

    private val logger = LoggerFactory.logger

    var dbChangeSubject: PublishSubject<SongsDb> = PublishSubject.create()
    private var saveRequestSubject: PublishSubject<Boolean> = PublishSubject.create()

    var songsDb: SongsDb? = null
    private var generalSongsDao: GeneralSongsDao? = null
    val unlockedSongsDao: UnlockedSongsDao get() = userDataDao.get().unlockedSongsDao!!
    val favouriteSongsDao: FavouriteSongsDao get() = userDataDao.get().favouriteSongsDao!!
    val customSongsDao: CustomSongsDao get() = userDataDao.get().customSongsDao!!
    val playlistDao: PlaylistDao get() = userDataDao.get().playlistDao!!
    val openHistoryDao: OpenHistoryDao get() = userDataDao.get().openHistoryDao!!
    val exclusionDao: ExclusionDao get() = userDataDao.get().exclusionDao!!

    init {
        DaggerIoc.factoryComponent.inject(this)

        saveRequestSubject
                .debounce(1500, TimeUnit.MILLISECONDS)
                .subscribe { toSave ->
                    if (toSave)
                        save()
                }.isDisposed
    }

    fun init() {
        try {
            reloadSongsDb()
        } catch (t: Exception) {
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
        generalSongsDao?.close()
        userDataDao.get().save()
    }

    @Synchronized
    fun reloadSongsDb() {
        val versionNumber = try {
            loadGeneralData()
        } catch (t: Exception) {
            logger.error("failed to load general data", t)
            resetGeneralData()
            loadGeneralData()
        }

        try {
            userDataDao.get().read()
        } catch (t: Exception) {
            logger.error("failed to load user data", t)
            resetUserData()
            userDataDao.get().read()
        }

        val newSongsDb = SongsDbBuilder(versionNumber, generalSongsDao!!, userDataDao.get()).build()
        refillCategoryDisplayNames(newSongsDb)

        songsDb = newSongsDb
        dbChangeSubject.onNext(songsDb!!)
    }

    private fun loadGeneralData(): Long {
        localDbService.get().ensureLocalDbExists()
        val dbFile = localDbService.get().songsDbFile
        generalSongsDao = GeneralSongsDao(dbFile)
        val versionNumber = generalSongsDao?.readDbVersionNumber()
                ?: throw RuntimeException("invalid local songs database format")
        generalSongsDao!!.verifyDbVersion(versionNumber)
        return versionNumber
    }

    private fun refillCategoryDisplayNames(songsDb: SongsDb) {
        songsDb.categories.forEach { category ->
            category.displayName = when {
                category.type.localeStringId != null ->
                    uiResourceService.get().resString(category.type.localeStringId)
                else -> category.name
            }
        }
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
        generalSongsDao?.close()
        localDbService.get().factoryReset()
    }

    fun resetUserData() {
        logger.warn("resetting user data...")
        userDataDao.get().factoryReset()
    }

    @Synchronized
    fun songsDbVersion(): Long? {
        return generalSongsDao?.readDbVersionNumber()
    }

    fun reloadUserData() {
        userDataDao.get().save()
        reloadSongsDb()
    }

}
