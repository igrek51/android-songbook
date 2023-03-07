package igrek.songbook.persistence.repository

import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.general.dao.PublicSongsDao
import igrek.songbook.persistence.general.model.Category
import igrek.songbook.persistence.general.model.CategoryType
import igrek.songbook.persistence.repository.builder.CustomSongsDbBuilder
import igrek.songbook.persistence.repository.builder.PublicSongsDbBuilder
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.persistence.user.custom.CustomSongsDao
import igrek.songbook.persistence.user.favourite.FavouriteSongsDao
import igrek.songbook.persistence.user.history.OpenHistoryDao
import igrek.songbook.persistence.user.playlist.PlaylistDao
import igrek.songbook.persistence.user.songtweak.SongTweakDao
import igrek.songbook.persistence.user.transpose.TransposeDao
import igrek.songbook.persistence.user.unlocked.UnlockedSongsDao
import igrek.songbook.util.lookup.SimpleCache
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Singleton
class SongsRepository(
    localDbService: LazyInject<LocalDbService> = appFactory.localDbService,
    userDataDao: LazyInject<UserDataDao> = appFactory.userDataDao,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
) {
    private val localDbService by LazyExtractor(localDbService)
    private val userDataDao by LazyExtractor(userDataDao)
    private val uiResourceService by LazyExtractor(uiResourceService)

    private val logger = LoggerFactory.logger

    var dbChangeSubject: PublishSubject<Boolean> = PublishSubject.create()

    var publicSongsRepo: PublicSongsRepository =
        PublicSongsRepository(0, SimpleCache.emptyList(), SimpleCache.emptyList())
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
    private val dataTransferMutex = Mutex()

    val unlockedSongsDao: UnlockedSongsDao get() = userDataDao.unlockedSongsDao
    val favouriteSongsDao: FavouriteSongsDao get() = userDataDao.favouriteSongsDao
    val customSongsDao: CustomSongsDao get() = userDataDao.customSongsDao
    val playlistDao: PlaylistDao get() = userDataDao.playlistDao
    val openHistoryDao: OpenHistoryDao get() = userDataDao.openHistoryDao
    val transposeDao: TransposeDao get() = userDataDao.transposeDao
    val songTweakDao: SongTweakDao get() = userDataDao.songTweakDao

    suspend fun reloadSongsDb() {
        dataTransferMutex.withLock {
            val versionNumber = try {
                loadPublicSongsData()
            } catch (t: Throwable) {
                logger.error("failed to load version from general songs data, resetting", t)
                resetGeneralSongsData()
                loadPublicSongsData()
            }

            publicSongsRepo = try {
                val publicDbBuilder =
                    PublicSongsDbBuilder(versionNumber, publicSongsDao!!, userDataDao)
                publicDbBuilder.buildPublic(uiResourceService)
            } catch (t: Throwable) {
                logger.error("failed to load public songs data", t)
                resetGeneralSongsData()
                loadPublicSongsData()
                val publicDbBuilder =
                    PublicSongsDbBuilder(versionNumber, publicSongsDao!!, userDataDao)
                publicDbBuilder.buildPublic(uiResourceService)
            }

            val customDbBuilder = CustomSongsDbBuilder(userDataDao)
            customSongsRepo = customDbBuilder.buildCustom(uiResourceService)

            allSongsRepo = AllSongsRepository(publicSongsRepo, customSongsRepo)
            dbChangeSubject.onNext(true)
        }
    }

    private suspend fun reloadCustomSongsDb() {
        dataTransferMutex.withLock {
            val customDbBuilder = CustomSongsDbBuilder(userDataDao)
            customSongsRepo = customDbBuilder.buildCustom(uiResourceService)

            allSongsRepo = AllSongsRepository(publicSongsRepo, customSongsRepo)
            dbChangeSubject.onNext(true)
        }
    }

    private fun loadPublicSongsData(): Long {
        localDbService.ensureLocalDbExists()
        val dbFile = localDbService.songsDbFile
        publicSongsDao = PublicSongsDao(dbFile)
        val versionNumber = publicSongsDao?.readDbVersionNumber()
            ?: throw RuntimeException("invalid local songs database format")
        publicSongsDao!!.verifyDbVersion(versionNumber)
        return versionNumber
    }

    fun fullFactoryReset() {
        runBlocking(Dispatchers.IO) {
            dataTransferMutex.withLock {
                resetGeneralSongsData()
                userDataDao.factoryReset()
                reloadSongsDb()
            }
        }
    }

    fun resetGeneralSongsData() {
        logger.warn("Resetting general songs data...")
        publicSongsDao?.close()
        localDbService.factoryReset()
    }

    fun songsDbVersion(): Long? {
        return publicSongsDao?.readDbVersionNumber()
    }

    suspend fun close() {
        dataTransferMutex.withLock {
            publicSongsDao?.close()
        }
    }

    fun saveAndReloadUserSongs() {
        runBlocking(Dispatchers.IO) {
            userDataDao.save()
            reloadCustomSongsDb()
        }
    }

    suspend fun saveAndReloadAllSongs() {
        userDataDao.save()
        reloadSongsDb()
    }

}
