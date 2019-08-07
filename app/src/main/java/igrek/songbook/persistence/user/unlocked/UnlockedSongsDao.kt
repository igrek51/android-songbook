package igrek.songbook.persistence.user.unlocked

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao
import javax.inject.Inject

class UnlockedSongsDao(path: String) : AbstractJsonDao<UnlockedSongsDb>(
        path,
        dbName = "unlocked",
        schemaVersion = 2,
        clazz = UnlockedSongsDb::class.java,
        serializer = UnlockedSongsDb.serializer()
) {

    val unlockedSongs: UnlockedSongsDb get() = db!!

    @Inject
    lateinit var songsRepository: SongsRepository
    @Inject
    lateinit var activity: Activity

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    override fun empty(): UnlockedSongsDb {
        return UnlockedSongsDb(mutableListOf())
    }

    override fun migrateOlder(): UnlockedSongsDb? {
        return null
    }

    fun unlockKey(key: String) {
        val keys = unlockedSongs.keys
        if (key !in keys)
            keys.add(key)
        songsRepository.reloadUserData()
    }

}