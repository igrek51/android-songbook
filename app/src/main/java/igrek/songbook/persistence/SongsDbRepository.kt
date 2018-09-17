package igrek.songbook.persistence

import com.google.common.collect.ArrayListMultimap
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.domain.songsdb.Song
import igrek.songbook.domain.songsdb.SongCategory
import igrek.songbook.domain.songsdb.SongsDb
import igrek.songbook.logger.LoggerFactory
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import io.reactivex.subjects.PublishSubject
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

    var dbChangeSubject: PublishSubject<SongsDb> = PublishSubject.create()

    var songsDb: SongsDb? = null
        private set

    private val logger = LoggerFactory.getLogger()

    init {
        DaggerIoc.getFactoryComponent().inject(this)
        localDatabaseService.checkDatabaseValid()
        initializeSongsDb()
    }

    fun recreateDb() {
        localDatabaseService.recreateDb()
        initializeSongsDb()
        uiInfoService.showInfo(R.string.ui_db_is_uptodate)
    }

    fun reloadDb() {
        initializeSongsDb()
    }

    private fun initializeSongsDb() {
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

        dbChangeSubject.onNext(songsDb!!)
    }

}
