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

    private val latestCompatibleDbVersion = 28 // db could not to be latest, but migration not necessary

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun checkDbVersion(songsRepository: SongsRepository) {
        this.songsRepository = songsRepository
        try {
            // check songs database version
            val songsDbVersion = songsRepository.songsDao.readDbVersionNumber()
            if (isLatest(songsDbVersion)) {
                verifyLocalDb(songsRepository)
                return
            }
            // migration needed
            logger.info("Db version incompatible - local: $songsDbVersion, latest compatibility: $latestCompatibleDbVersion")
            // very old db - factory reset
            if (songsDbVersion == null || songsDbVersion < 18) {
                throw RuntimeException("db version very old - factory reset needed")
            }
            // migration #028 needed
            if (songsDbVersion < 28) {
                makeMigratePublicLocalDb()
            }

            verifyLocalDb(songsRepository)

        } catch (t: Throwable) {
            logger.error(t)
            // any exception causes factory reset
            songsRepository.factoryReset()
        }
    }

    private fun verifyLocalDb(songsRepository: SongsRepository): Long? {
        // custom songs is part of local database
        val localSongsDbVersion = songsRepository.customSongsDao.readDbVersionNumber()
        if (localSongsDbVersion == null)
            throw RuntimeException("local db version error")
        return localSongsDbVersion
    }

    private fun isLatest(songsDbVersion: Long?): Boolean {
        return songsDbVersion != null && songsDbVersion >= latestCompatibleDbVersion
    }

    fun makeFactoryReset() {
        songsRepository!!.factoryReset()
    }

    private fun makeMigratePublicLocalDb() {
        logger.info("Applying migration #028: public + local dbs")
        Migration028PublicLocalDb(activity).migrate(this)
    }
}