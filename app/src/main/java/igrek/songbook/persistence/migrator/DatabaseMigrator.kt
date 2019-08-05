package igrek.songbook.persistence.migrator

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
        DaggerIoc.factoryComponent.inject(this)
    }

    fun verifyLocalDbVersion(songsRepository: SongsRepository) {
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

    private fun customDbVersion(): Long {
        TODO("not implemented")
    }

    fun verifySongsDbVersion(localDbService: LocalDbService) {
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

    private fun migrate37() {
        logger.info("Applying migration #037: metre, autoscroll columns")
        Migration037PublicLocalDb(activity).migrate(this)
    }
}