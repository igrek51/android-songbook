package igrek.songbook.persistence.user

import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.user.custom.CustomSongsDao
import igrek.songbook.persistence.user.favourite.FavouriteSongsDao
import igrek.songbook.persistence.user.unlocked.UnlockedSongsDao
import javax.inject.Inject


class UserDataDao {

    @Inject
    lateinit var localDbService: Lazy<LocalDbService>

    var unlockedSongsDao: UnlockedSongsDao? = null
    var favouriteSongsDao: FavouriteSongsDao? = null
    var customSongsDao: CustomSongsDao? = null

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun read() {
        val path = localDbService.get().songDbDir.absolutePath

        // TODO read latest data first, then migrate olders
        unlockedSongsDao = UnlockedSongsDao(path)
        favouriteSongsDao = FavouriteSongsDao(path)
        customSongsDao = CustomSongsDao(path)
    }

    fun save() {
        unlockedSongsDao?.save()
        favouriteSongsDao?.save()
        customSongsDao?.save()
    }

}