package igrek.songbook.persistence.user.device

import igrek.songbook.persistence.user.AbstractJsonDao

class DeviceDao(
    path: String,
    resetOnError: Boolean = false,
) : AbstractJsonDao<DeviceDb>(
    path,
    dbName = "device",
    schemaVersion = 1,
    clazz = DeviceDb::class.java,
    serializer = DeviceDb.serializer(),
) {
    val deviceDb: DeviceDb get() = db!!

    init {
        read(resetOnError)
    }

    override fun empty(): DeviceDb {
        return DeviceDb()
    }
}