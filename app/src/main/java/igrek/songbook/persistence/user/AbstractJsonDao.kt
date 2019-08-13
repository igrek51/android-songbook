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

    private fun readDb(): T {
        for (attemptSchema in schemaVersion downTo 1) {
            if (attemptSchema < schemaVersion)
                logger.debug("'$dbName' db: trying to read older version $attemptSchema...")

            try {
                return readFromFile(dbName, attemptSchema)
            } catch (e: FileNotFoundException) {
                // logger.debug("'$dbName' db: database v$attemptSchema not found")
            } catch (e: SerializationException) {
                logger.error("'$dbName' db: JSON deserialization error", e)
            } catch (e: Exception) {
                logger.error("'$dbName' db: error reading '$dbName db'", e)
            }
        }

        try {
            val oldDb = migrateOlder()
            if (oldDb != null)
                return oldDb
        } catch (e: Exception) {
            logger.error("'$dbName' db: failed to migrate older db", e)
        }

        logger.debug("'$dbName' db: loading empty db...")
        return empty()
    }

    private fun readFromFile(dbName: String, schemaVersion: Int): T {
        val filename = buildFilename(dbName, schemaVersion)
        val file = File(path, filename)
        if (!file.exists())
            throw FileNotFoundException("file not found: ${file.absoluteFile}")

        val content = file.readText(Charsets.UTF_8)
        val parsed = json.parse(serializer, content)
        logger.debug("'$dbName' db has been loaded from ${file.absoluteFile}")
        return parsed
    }

    open fun read() {
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