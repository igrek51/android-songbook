package igrek.songbook.persistence

import com.google.common.collect.ArrayListMultimap
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.domain.songsdb.Song
import igrek.songbook.domain.songsdb.SongCategory
import igrek.songbook.domain.songsdb.SongsDb
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
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
        checkDbVersion()
    }

    private fun checkDbVersion() {
        // check migrations
        val songsDbVersion = songsDao.readDbVersionNumber()
        if (songsDbVersion == null || songsDbVersion < 1) {
            factoryReset()
        }
    }

    fun factoryReset() {
        localDbService.factoryResetDbs()
        initializeSongsDb()
    }

    fun updateSongsDb() {
        initializeSongsDb()
        uiInfoService.showInfo(R.string.ui_db_is_uptodate)
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

        // unlock songs
        val unlockedSongIds: List<Long> = unlockedSongsDao.readUnlockedSongIds()
        for (unlockedSongId in unlockedSongIds) {
            val song: Song? = songIds[unlockedSongId]
            if (song != null) {
                song.locked = false
            }
        }

        // add custom songs
        val customSongs = customSongsDao.readAllSongs(categories)
        songs.addAll(customSongs)

        songsDb = SongsDb(versionNumber, categories, songs)

        dbChangeSubject.onNext(songsDb!!)
    }

    fun unlockSong(songId: Long) {
        unlockedSongsDao.unlockSong(songId)
    }

    fun saveImportedSong(song: Song) {
        // TODO add or update
    }

    fun removeImportedSong(song: Song) {
        // TODO
    }

    fun getSongsDbVersion(): Long? {
        return songsDao.readDbVersionNumber()
    }

}
