package igrek.songbook.persistence

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.dao.SongsDao
import igrek.songbook.persistence.migration.DatabaseMigrator
import igrek.songbook.persistence.model.SongsDb
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class SongsRepository {

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

    private fun songsDao() = SongsDao(localDbService.get().openSongsDb())

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
        val newSongsDb = SongsDbBuilder(songsDao()).build()
        refillCategoryDisplayNames(newSongsDb)

        songsDb = newSongsDb
        dbChangeSubject.onNext(songsDb!!)
    }

    private fun refillCategoryDisplayNames(songsDb: SongsDb) {
        songsDb.categories.forEach { category ->
            category.displayName = category.name
                    ?: uiResourceService.get().resString(category.type.localeStringId!!)
        }
    }

    fun factoryReset() {
        logger.warn("Databases factory reset...")
        localDbService.get().factoryReset()
        reloadSongsDb()
    }

    fun getSongsDbVersion(): Long? {
        return songsDao().readDbVersionNumber()
    }

}
