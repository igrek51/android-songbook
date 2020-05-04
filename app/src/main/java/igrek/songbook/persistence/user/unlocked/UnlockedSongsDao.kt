package igrek.songbook.persistence.user.unlocked

import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.AbstractJsonDao

class UnlockedSongsDao(
        path: String,
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
) : AbstractJsonDao<UnlockedSongsDb>(
        path,
        dbName = "unlocked",
        schemaVersion = 1,
        clazz = UnlockedSongsDb::class.java,
        serializer = UnlockedSongsDb.serializer()
) {
    private val songsRepository by LazyExtractor(songsRepository)

    val unlockedSongs: UnlockedSongsDb get() = db!!

    init {
        read()
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
        songsRepository.saveDataReloadAllSongs()
    }

}