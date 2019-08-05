package igrek.songbook.persistence.user.unlocked

import igrek.songbook.persistence.user.AbstractUserDataService

class UnlockedSongsDbService(path: String) : AbstractUserDataService<UnlockedSongsDb>(
        path,
        dbName = "unlocked",
        schemaVersion = 2,
        clazz = UnlockedSongsDb::class.java,
        serializer = UnlockedSongsDb.serializer()
) {

    override fun empty(): UnlockedSongsDb {
        return UnlockedSongsDb(emptyList())
    }

}