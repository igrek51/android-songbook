package igrek.songbook.persistence

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.dao.SongsDao
import igrek.songbook.persistence.migration.DatabaseMigrator
import igrek.songbook.persistence.model.Category
import igrek.songbook.persistence.model.SongCategoryRelationship
import igrek.songbook.persistence.model.SongsDb
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class SongsRepository {

    lateinit var songsDao: SongsDao
    @Inject
    lateinit var localDbService: Lazy<LocalDbService>
    @Inject
    lateinit var uiResourceService: Lazy<UiResourceService>
    @Inject
    lateinit var uiInfoService: Lazy<UiInfoService>
    @Inject
    lateinit var songsUpdater: Lazy<SongsUpdater>
    @Inject
    lateinit var databaseMigrator: Lazy<DatabaseMigrator>

    private val logger = LoggerFactory.logger

    var dbChangeSubject: PublishSubject<SongsDb> = PublishSubject.create()

    var songsDb: SongsDb? = null
        private set
        get() {
            if (field == null)
                init()
            return field
        }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun init() {
        try {
            reloadSongsDb()
        } catch (t: Throwable) {
            factoryReset()
        }
    }

    fun reloadSongsDb() {
        val newSongsDb: SongsDb = loadSongs()

        loadUserData(newSongsDb)

        postProcessSongs(newSongsDb)

        songsDb = newSongsDb
        dbChangeSubject.onNext(songsDb!!)
    }

    fun factoryReset() {
        logger.warn("Databases factory reset...")
        localDbService.get().factoryReset()
        reloadSongsDb()
    }

    private fun loadUserData(songsDb: SongsDb) {
        // TODO latest data first, then migrate olders
//        loadCustomSongs(songsDb)

//        loadFavourites(songsDb)

        //unlockSongs(songsDb)

//        loadPlaylists(songsDb)
    }

    private fun loadSongs(): SongsDb {
        songsDao = SongsDao(localDbService.get().openSongsDb())
        val songsDb = createSongsDb(songsDao)

        val songCategories = songsDao.readAllSongCategories()
        assignSongsToCategories(songsDb, songCategories)

        // refill category display name
        songsDb.categories.forEach { category ->
            category.displayName = refillCategoryDisplayName(category)
        }

        return songsDb
    }

    private fun refillCategoryDisplayName(category: Category): String {
        return category.name ?: uiResourceService.get().resString(category.type.localeStringId!!)
    }

    private fun postProcessSongs(songsDb: SongsDb) {
        removeLockedSongs(songsDb)
        removeEmptyCategories(songsDb)
    }

    private fun removeLockedSongs(songsDb: SongsDb) {
        songsDb.songs = songsDb.songs.filter { song -> !song.locked }
    }

    private fun removeEmptyCategories(songsDb: SongsDb) {
        songsDb.categories = songsDb.categories.filter { category -> category.songs.isNotEmpty() }
    }

    private fun assignSongsToCategories(songsDb: SongsDb, songCategories: List<SongCategoryRelationship>) {
        songCategories.forEach { scRelation ->
            val song = songsDb.songFinder.find(scRelation.song_id)
            val category = songsDb.categoryFinder.find(scRelation.category_id)
            if (song != null && category != null) {
                song.categories.add(category)
                category.songs.add(song)
            }
        }
    }

    private fun createSongsDb(songsDao: SongsDao): SongsDb {
        val versionNumber = songsDao.readDbVersionNumber()
                ?: throw RuntimeException("invalid songs database format")

        val categories = songsDao.readAllCategories()
        val songs = songsDao.readAllSongs()

        return SongsDb(versionNumber, categories, songs)
    }

    fun getSongsDbVersion(): Long? {
        return songsDao.readDbVersionNumber()
    }

}
