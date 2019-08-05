package igrek.songbook.persistence.user

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.user.custom.CustomSongsDb
import igrek.songbook.persistence.user.custom.CustomSongsDbService
import igrek.songbook.persistence.user.favourite.FavouriteSongsDb
import igrek.songbook.persistence.user.favourite.FavouriteSongsDbService
import igrek.songbook.persistence.user.unlocked.UnlockedSongsDb
import igrek.songbook.persistence.user.unlocked.UnlockedSongsDbService
import javax.inject.Inject


class UserDataService {

    @Inject
    lateinit var localDbService: Lazy<LocalDbService>

    private var unlockedSongsDbService: UnlockedSongsDbService? = null
    private var favouriteSongsDbService: FavouriteSongsDbService? = null
    private var customSongsDbService: CustomSongsDbService? = null

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    var unlockedSongs: UnlockedSongsDb? = null
        private set
    var favouriteSongs: FavouriteSongsDb? = null
        private set
    var customSongs: CustomSongsDb? = null
        private set

    fun read() {
        val path = localDbService.get().songDbDir.absolutePath

        // TODO latest data first, then migrate olders

        unlockedSongsDbService = UnlockedSongsDbService(path)
        favouriteSongsDbService = FavouriteSongsDbService(path)
        customSongsDbService = CustomSongsDbService(path)

        unlockedSongs = unlockedSongsDbService?.read()
        favouriteSongs = favouriteSongsDbService?.read()
        customSongs = customSongsDbService?.read()
    }

    fun save() {
        if (unlockedSongs != null)
            unlockedSongsDbService?.save(unlockedSongs!!)
        if (favouriteSongs != null)
            favouriteSongsDbService?.save(favouriteSongs!!)
        if (favouriteSongs != null)
            customSongsDbService?.save(customSongs!!)
    }

}