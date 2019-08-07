package igrek.songbook.persistence.user

import igrek.songbook.info.logger.LoggerFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.File
import java.io.FileNotFoundException

abstract class AbstractJsonDao<T>(
        private val path: String,
        val dbName: String,
        val schemaVersion: Int,
        val clazz: Class<T>,
        val serializer: KSerializer<T>
) {

    private val json = Json(JsonConfiguration.Stable)
    private val logger = LoggerFactory.logger

    protected var db: T? = null

    abstract fun empty(): T

    open fun migrateOlder(): T? {
        return null
    }

    init {
        read()
    }

    private fun readDb(): T {
        for (attemptSchema in schemaVersion downTo 1) {
            if (attemptSchema < schemaVersion)
                logger.info("trying to read older db version $attemptSchema for $dbName...")

            try {
                return readFromFile(dbName, attemptSchema)
            } catch (e: FileNotFoundException) {
                logger.error("database file not found for db $dbName")
            } catch (e: SerializationException) {
                logger.error("JSON reading error for db $dbName: ${e.message}")
            } catch (e: Throwable) {
                logger.error("error reading db $dbName: ${e.message}")
            }
        }

        try {
            val oldDb = migrateOlder()
            if (oldDb != null)
                return oldDb
        } catch (e: Throwable) {
            logger.error("failed to migrate older db $dbName: ${e.message}")
        }

        logger.info("db $dbName not found, loading empty")
        return empty()
    }

    private fun readFromFile(dbName: String, schemaVersion: Int): T {
        val filename = buildFilename(dbName, schemaVersion)
        val file = File(path, filename)
        if (!file.exists())
            throw FileNotFoundException()

        val content = file.readText(Charsets.UTF_8)
        return json.parse(serializer, content)
    }

    private fun read() {
        db = readDb()
    }

    fun save() {
        if (db != null)
            saveDb(db!!)
    }

    private fun saveDb(db: T) {
        saveToFile(dbName, schemaVersion, db)
    }

    private fun saveToFile(dbName: String, schemaVersion: Int, obj: T) {
        json.toJson(serializer, obj)
        val content = json.stringify(serializer, obj)
        val filename = buildFilename(dbName, schemaVersion)
        val file = File(path, filename)
        file.writeText(content, Charsets.UTF_8)
        logger.debug("db $dbName saved to file ${file.absolutePath}")
    }

    private fun buildFilename(name: String, schemaVersion: Int): String {
        return "$name.$schemaVersion.json"
    }

    fun factoryReset() {
        val filename = buildFilename(dbName, schemaVersion)
        val file = File(path, filename)
        if (!file.delete() || file.exists())
            logger.error("failed to delete json db file: " + file.absolutePath)
        read()
    }
}