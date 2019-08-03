package igrek.songbook.persistence

import com.google.common.collect.ArrayListMultimap
import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.dao.CustomSongsDao
import igrek.songbook.persistence.dao.SongsDao
import igrek.songbook.persistence.dao.UnlockedSongsDao
import igrek.songbook.persistence.migration.DatabaseMigrator
import igrek.songbook.persistence.songsdb.Song
import igrek.songbook.persistence.songsdb.SongCategory
import igrek.songbook.persistence.songsdb.SongsDb
import igrek.songbook.songselection.favourite.FavouriteSongsRepository
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class SongsRepository {

    @Inject
    lateinit var songsDao: Lazy<SongsDao>
    @Inject
    lateinit var customSongsDao: Lazy<CustomSongsDao>
    @Inject
    lateinit var unlockedSongsDao: Lazy<UnlockedSongsDao>
    @Inject
    lateinit var favouriteSongsRepository: Lazy<FavouriteSongsRepository>
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
            localDbService.get().openSongsDb()

            loadSongs()

            loadUserData()
        } catch (t: Throwable) {
            factoryReset()
        }
    }

    fun factoryReset() {
        logger.warn("Databases factory reset...")
        localDbService.get().factoryReset()
        buildSongsDb()
    }

    private fun updateSongsDb() {
        songsUpdater.get().updateSongsDb(localDbService.get().songsDbFile)
    }

    private fun loadUserData() {
        // TODO latest data first, then migrate olders
    }


    private fun loadSongs() {
        val versionNumber = songsDao.get().readDbVersionNumber()
                ?: throw RuntimeException("invalid songs database format")

        val categories = songsDao.get().readAllCategories()
        val songs = songsDao.get().readAllSongs(categories)

        // group by categories
        val categorySongs = ArrayListMultimap.create<SongCategory, Song>()
        val songIds = HashMap<Long, Song>()
        for (song in songs) {
            categorySongs.put(song.category, song)
            songIds[song.id] = song
        }

        // unlock songs
        val unlockedKeys: List<String> = unlockedSongsDao.get().readUnlockedKeys()
        for (unlockedKey in unlockedKeys) {
            for (song in songs) {
                if (song.lockPassword == unlockedKey)
                    song.locked = false
            }
        }

        // add custom songs
        val customSongs = customSongsDao.get().readAllSongs(categories)
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
                val displayName = uiResourceService.get().resString(category.type
                        .localeStringId!!)
                category.displayName = displayName
            }
        }

        songsDb = SongsDb(versionNumber, categories, songs)

        favouriteSongsRepository.get().resetCache()

        dbChangeSubject.onNext(songsDb!!)
    }

    fun unlockKey(key: String) {
        unlockedSongsDao.get().unlockKey(key)
    }

    fun addCustomSong(song: Song) {
        customSongsDao.get().addCustomSong(song)
        initializeSongsDb()
    }

    fun removeCustomSong(song: Song) {
        customSongsDao.get().removeCustomSong(song)
        // TODO remove any other places as well (favourites list)
        initializeSongsDb()
    }

    fun updateCustomSong(song: Song) {
        customSongsDao.get().updateCustomSong(song)
        initializeSongsDb()
    }

    fun getSongsDbVersion(): Long? {
        return songsDao.get().readDbVersionNumber()
    }

    fun getCustomCategoryByTypeId(categoryTypeId: Long): SongCategory? {
        return customSongsDao.get().getCategoryByTypeId(categoryTypeId)
    }

}
