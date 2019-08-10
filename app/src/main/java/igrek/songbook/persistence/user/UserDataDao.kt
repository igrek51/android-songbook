package igrek.songbook.persistence.user

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.user.custom.CustomSongsDao
import igrek.songbook.persistence.user.favourite.FavouriteSongsDao
import igrek.songbook.persistence.user.playlist.PlaylistDao
import igrek.songbook.persistence.user.unlocked.UnlockedSongsDao
import javax.inject.Inject


class UserDataDao {

    @Inject
    lateinit var localDbService: Lazy<LocalDbService>

    var unlockedSongsDao: UnlockedSongsDao? = null
    var favouriteSongsDao: FavouriteSongsDao? = null
    var customSongsDao: CustomSongsDao? = null
    var playlistDao: PlaylistDao? = null

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
    }

    fun save() {
        unlockedSongsDao?.save()
        favouriteSongsDao?.save()
        customSongsDao?.save()
        playlistDao?.save()
        logger.info("user data have been saved")
    }

}