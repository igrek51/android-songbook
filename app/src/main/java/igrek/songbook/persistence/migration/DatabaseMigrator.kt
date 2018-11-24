package igrek.songbook.persistence.migration

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.SongsRepository
import javax.inject.Inject


class DatabaseMigrator {

    @Inject
    lateinit var activity: Activity

    private val logger = LoggerFactory.getLogger()
    var songsRepository: SongsRepository? = null

    private val latestDbVersion = 28
    private val minDbVersionCompatibility = 18

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun checkDbVersion(songsRepository: SongsRepository) {
        this.songsRepository = songsRepository
        try {
            // check migrations
            val songsDbVersion = songsRepository.songsDao.readDbVersionNumber()
            val localSongsDbVersion = songsRepository.customSongsDao.readDbVersionNumber()

            if (isLatest(songsDbVersion, localSongsDbVersion)) {
                return
            }
            logger.info("Db version incompatible - local: $localSongsDbVersion, remote: $songsDbVersion")

            if (factoryResetNeeded(songsDbVersion, localSongsDbVersion)) {
                songsRepository.factoryReset()
                return
            }
            if (songsDbVersion!! < 28) {
                makeMigratePublicLocalDb(songsRepository)
            }

        } catch (t: Throwable) {
            logger.error(t)
            // any exception causes factory reset
            songsRepository.factoryReset()
        }
    }

    private fun isLatest(songsDbVersion: Long?, localSongsDbVersion: Long?): Boolean {
        return songsDbVersion != null && localSongsDbVersion != null && songsDbVersion >= latestDbVersion
    }

    private fun factoryResetNeeded(songsDbVersion: Long?, localSongsDbVersion: Long?): Boolean {
        return songsDbVersion == null || localSongsDbVersion == null || songsDbVersion < minDbVersionCompatibility
    }

    fun makeFactoryReset() {
        songsRepository!!.factoryReset()
    }

    private fun makeMigratePublicLocalDb(songsRepository: SongsRepository) {
        logger.debug("applying migration: public + local dbs")
        Migration028PublicLocalDb(activity).migrate(this)
    }
}