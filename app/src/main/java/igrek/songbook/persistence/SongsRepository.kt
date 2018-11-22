package igrek.songbook.persistence

import com.google.common.collect.ArrayListMultimap
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.model.songsdb.Song
import igrek.songbook.model.songsdb.SongCategory
import igrek.songbook.model.songsdb.SongsDb
import igrek.songbook.persistence.migration.DatabaseMigrator
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class SongsRepository {

    @Inject
    lateinit var songsDao: SongsDao
    @Inject
    lateinit var customSongsDao: CustomSongsDao
    @Inject
    lateinit var unlockedSongsDao: UnlockedSongsDao
    @Inject
    lateinit var localDbService: LocalDbService
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var songsUpdater: SongsUpdater
    @Inject
    lateinit var databaseMigrator: DatabaseMigrator

    private val logger = LoggerFactory.getLogger()

    var dbChangeSubject: PublishSubject<SongsDb> = PublishSubject.create()

    var songsDb: SongsDb? = null
        private set
        get() {
            if (field == null)
                initializeSongsDb()
            return field
        }

    init {
        DaggerIoc.getFactoryComponent().inject(this)
        databaseMigrator.checkDbVersion(this)
    }

    fun factoryReset() {
        logger.warn("Databases factory reset...")
        localDbService.factoryResetDbs()
        initializeSongsDb()
    }

    fun updateSongsDb() {
        songsUpdater.updateSongsDb(localDbService.songsDbFile)
    }

    fun initializeSongsDb() {
        val versionNumber = songsDao.readDbVersionNumber()
                ?: throw RuntimeException("invalid songs database format")

        val categories = songsDao.readAllCategories()
        val songs = songsDao.readAllSongs(categories)

        // group by categories
        val categorySongs = ArrayListMultimap.create<SongCategory, Song>()
        val songIds = HashMap<Long, Song>()
        for (song in songs) {
            categorySongs.put(song.category, song)
            songIds[song.id] = song
        }

        // unlock songs
        val unlockedKeys: List<String> = unlockedSongsDao.readUnlockedKeys()
        for (unlockedKey in unlockedKeys) {
            for (song in songs) {
                if (song.lockPassword == unlockedKey)
                    song.locked = false
            }
        }

        // add custom songs
        val customSongs = customSongsDao.readAllSongs(categories)
        if (customSongs.isNotEmpty()) {
            songs.addAll(customSongs)
            // add custom category
            for (song in customSongs) {
                categorySongs.put(song.category, song)
            }
        }

        // build category tree
        for (category in categories) {
            val songsOfCategory = categorySongs.get(category)
            category.songs = ArrayList(songsOfCategory)
            // refill category display name
            if (category.name != null) {
                category.displayName = category.name
            } else {
                val displayName = uiResourceService.resString(category.type
                        .localeStringId!!)
                category.displayName = displayName
            }
        }

        songsDb = SongsDb(versionNumber, categories, songs)

        dbChangeSubject.onNext(songsDb!!)
    }

    fun unlockKey(key: String) {
        unlockedSongsDao.unlockKey(key)
    }

    fun saveImportedSong(song: Song) {
        customSongsDao.saveCustomSong(song)
        initializeSongsDb()
    }

    fun removeCustomSong(song: Song) {
        customSongsDao.removeCustomSong(song)
        initializeSongsDb()
    }

    fun updateCustomSong(song: Song) {
        customSongsDao.updateCustomSong(song)
        initializeSongsDb()
    }

    fun getSongsDbVersion(): Long? {
        return songsDao.readDbVersionNumber()
    }

    fun getCustomCategoryByTypeId(categoryTypeId: Long): SongCategory? {
        return customSongsDao.getCategoryByTypeId(categoryTypeId)
    }

}
