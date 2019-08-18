package igrek.songbook.persistence.user

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.user.custom.CustomSongsDao
import igrek.songbook.persistence.user.exclusion.ExclusionDao
import igrek.songbook.persistence.user.favourite.FavouriteSongsDao
import igrek.songbook.persistence.user.playlist.OpenHistoryDao
import igrek.songbook.persistence.user.playlist.PlaylistDao
import igrek.songbook.persistence.user.transpose.TransposeDao
import igrek.songbook.persistence.user.unlocked.UnlockedSongsDao
import javax.inject.Inject


class UserDataDao {

    @Inject
    lateinit var localDbService: Lazy<LocalDbService>

    var unlockedSongsDao: UnlockedSongsDao? = null
    var favouriteSongsDao: FavouriteSongsDao? = null
    var customSongsDao: CustomSongsDao? = null
    var playlistDao: PlaylistDao? = null
    var openHistoryDao: OpenHistoryDao? = null
    var exclusionDao: ExclusionDao? = null
    var transposeDao: TransposeDao? = null

    private val logger = LoggerFactory.logger

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun read() {
        val path = localDbService.get().songDbDir.absolutePath

        unlockedSongsDao = UnlockedSongsDao(path)
        favouriteSongsDao = FavouriteSongsDao(path)
        customSongsDao = CustomSongsDao(path)
        playlistDao = PlaylistDao(path)
        openHistoryDao = OpenHistoryDao(path)
        exclusionDao = ExclusionDao(path)
        transposeDao = TransposeDao(path)
    }

    fun save() {
        unlockedSongsDao?.save()
        favouriteSongsDao?.save()
        customSongsDao?.save()
        playlistDao?.save()
        openHistoryDao?.save()
        exclusionDao?.save()
        transposeDao?.save()
        logger.info("user data have been saved")
    }

    fun factoryReset() {
        customSongsDao?.factoryReset()
        favouriteSongsDao?.factoryReset()
        unlockedSongsDao?.factoryReset()
        playlistDao?.factoryReset()
        openHistoryDao?.factoryReset()
        exclusionDao?.factoryReset()
        transposeDao?.factoryReset()
    }

}