package igrek.songbook.persistence

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import javax.inject.Inject


class DatabaseMigrator {

    @Inject
    lateinit var songsRepository: Lazy<SongsRepository>

    private val logger = LoggerFactory.getLogger()

    private val minDbVersionCompatibility = 18

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun migrationNeeded(songsDbVersion: Long?, localSongsDbVersion: Long?): Boolean {
        return songsDbVersion == null || localSongsDbVersion == null || songsDbVersion < minDbVersionCompatibility
    }
}