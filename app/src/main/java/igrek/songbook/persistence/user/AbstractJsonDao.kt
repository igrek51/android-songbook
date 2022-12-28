package igrek.songbook.persistence.user

import igrek.songbook.info.logger.LoggerFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException

abstract class AbstractJsonDao<T>(
    private val path: String,
    val dbName: String,
    val schemaVersion: Int,
    val clazz: Class<T>,
    val serializer: KSerializer<T>
) {

    private val json = Json {
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
        prettyPrint = false
        useArrayPolymorphism = false
    }
    protected val logger = LoggerFactory.logger

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
            } catch (e: Throwable) {
                logger.error("'$dbName' db: error reading '$dbName db'", e)
            }
        }

        try {
            val oldDb = migrateOlder()
            if (oldDb != null) {
                logger.info("'$dbName' db: migration from old db has been successfully finished")
                return oldDb
            }
        } catch (e: Throwable) {
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
        return json.decodeFromString(serializer, content)
    }

    open fun read() {
        db = readDb()
    }

    open fun save() {
        db?.let {
            saveDb(it)
        }
    }

    private fun saveDb(db: T) {
        saveToFile(dbName, schemaVersion, db)
    }

    private fun saveToFile(dbName: String, schemaVersion: Int, obj: T) {
        json.encodeToJsonElement(serializer, obj)
        val content = json.encodeToString(serializer, obj)
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
        val backupFile = File(path, "$filename.bak")

        if (file.exists()) {
            if (backupFile.exists()) {
                logger.error("removing previous backup file: " + backupFile.absolutePath)
                backupFile.delete()
            }

            file.renameTo(backupFile)

            when (file.exists()) {
                true -> logger.error("failed to rename json db file: " + file.absolutePath)
                false -> logger.info("json db file ${file.absolutePath} moved to backup ${backupFile.absolutePath}")
            }
        }

        db = empty()
        save()
    }
}
