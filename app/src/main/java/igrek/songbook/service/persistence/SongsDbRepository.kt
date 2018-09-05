package igrek.songbook.service.persistence

import com.google.common.collect.ArrayListMultimap
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.domain.songsdb.Song
import igrek.songbook.domain.songsdb.SongCategory
import igrek.songbook.domain.songsdb.SongsDb
import igrek.songbook.logger.LoggerFactory
import igrek.songbook.service.info.UiInfoService
import igrek.songbook.service.info.UiResourceService
import igrek.songbook.service.persistence.database.LocalDatabaseService
import igrek.songbook.service.persistence.database.SqlQueryService
import java.util.*
import javax.inject.Inject

class SongsDbRepository {

    @Inject
    lateinit var sqlQueryService: SqlQueryService
    @Inject
    lateinit var localDatabaseService: LocalDatabaseService
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var uiInfoService: UiInfoService

    var songsDb: SongsDb? = null
        private set

    private val logger = LoggerFactory.getLogger()

    init {
        DaggerIoc.getFactoryComponent().inject(this)
        localDatabaseService.checkDatabaseValid()
        initSongsDb()
    }

    fun updateDb() {
        localDatabaseService.recreateDb()
        initSongsDb()
        uiInfoService.showInfo(R.string.ui_db_is_uptodate)
    }

    fun reloadDb() {
        initSongsDb()
    }

    private fun initSongsDb() {
        val versionNumber = sqlQueryService.readDbVersionNumber()!!

        val categories = sqlQueryService.readAllCategories()
        val songs = sqlQueryService.readAllSongs(categories)

        // group by categories
        val categorySongs = ArrayListMultimap.create<SongCategory, Song>()
        for (song in songs) {
            categorySongs.put(song.category, song)
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

        songsDb = SongsDb(versionNumber, categories, songs)
    }

}
