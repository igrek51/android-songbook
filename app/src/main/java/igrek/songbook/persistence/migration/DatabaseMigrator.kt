package igrek.songbook.persistence.migration

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.SongsRepository
import javax.inject.Inject


class DatabaseMigrator {

    @Inject
    lateinit var activity: Activity

    private val logger = LoggerFactory.logger
    var songsRepository: SongsRepository? = null

    private val latestCompatibleDbVersion = 37 // db could not to be latest, but migration not necessary
    private val latestSongsDbFromResources = 37

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    private fun customDbVersion(): Long? {
        return songsRepository!!.customSongsDao.get().readDbVersionNumber()
    }

    fun verifyLocalDbVersion(songsRepository: SongsRepository, localDbService: LocalDbService) {
        this.songsRepository = songsRepository
        try {
            // check database version
            val localVersion = customDbVersion()
            if (localVersion != null && localVersion >= latestCompatibleDbVersion) {
                // everything is fine
                return
            }
            // migration needed
            logger.info("Db version incompatible - local: $localVersion, latest compatibility: $latestCompatibleDbVersion")
            // very old db - factory reset
            if (localVersion == null || localVersion < 18) {
                throw RuntimeException("db version very old - factory reset needed")
            }
            // migration #028 needed
            if (localVersion < 28) {
                migrate28PublicLocalDb()
            }
            if (localVersion < 37) {
                migrate37()
            }

            val newVersion = getLocalDbVersion()
            logger.info("Local database migrated to $newVersion")

        } catch (t: Throwable) {
            logger.error(t)
            // any exception causes factory reset
            songsRepository.factoryReset()
        }
    }

    fun verifySongsDbVersion(songsRepository: SongsRepository, localDbService: LocalDbService) {
        try {
            val songsVersion = customDbVersion()
            if (songsVersion == null || songsVersion < latestSongsDbFromResources) {
                throw RuntimeException("songs db on local disk has obsolete version: $songsVersion")
            }
        } catch (t: Throwable) {
            logger.warn("making factory reset for songs database: ${t.message}")
            localDbService.factoryResetSongsDb()
        }
    }

    private fun getLocalDbVersion(): Long {
        // custom songs is part of local database
        return customDbVersion() ?: throw RuntimeException("local db version error")
    }

    fun makeFactoryReset() {
        songsRepository!!.factoryReset()
    }

    private fun migrate28PublicLocalDb() {
        logger.info("Applying migration #028: public + local dbs")
        Migration028PublicLocalDb(activity).migrate(this)
    }

    private fun migrate37() {
        logger.info("Applying migration #037: metre, autoscroll columns")
        Migration037PublicLocalDb(activity).migrate(this)
    }
}