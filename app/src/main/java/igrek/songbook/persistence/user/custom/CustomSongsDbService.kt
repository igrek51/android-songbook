package igrek.songbook.persistence.user.custom

import igrek.songbook.persistence.user.AbstractUserDataService

class CustomSongsDbService(path: String) : AbstractUserDataService<CustomSongsDb>(
        path,
        dbName = "customsongs",
        schemaVersion = 2,
        clazz = CustomSongsDb::class.java,
        serializer = CustomSongsDb.serializer()
) {

    override fun empty(): CustomSongsDb {
        return CustomSongsDb(mutableListOf())
    }

}