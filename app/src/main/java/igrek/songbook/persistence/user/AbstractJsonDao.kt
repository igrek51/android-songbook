package igrek.songbook.persistence.user

import igrek.songbook.info.errorcheck.ContextError
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
        isLenient = false
    }
    protected val logger = LoggerFactory.logger

    protected var db: T? = null

    abstract fun empty(): T

    open fun migrateOlder(): T? {
        return null
    }

    private fun readDb(resetOnError: Boolean): T {
        try {
            return readFromFile(dbName, schemaVersion)

        } catch (e: FileNotFoundException) {
            logger.debug("No '$dbName' database: loading empty db...")
            return empty()

        } catch (e: SerializationException) {
            when (resetOnError) {
                false -> throw ContextError("'$dbName' database: JSON deserialization error", e)
                true -> {
                    logger.error("'$dbName' database: JSON deserialization error", e)
                    makeBackup()
                    logger.debug("'$dbName' database: loading empty db...")
                    return empty()
                }
            }

        } catch (e: Throwable) {
            when (resetOnError) {
                false -> throw ContextError("'$dbName' database: error reading local database", e)
                true -> {
                    logger.error("'$dbName' database: error reading local database", e)
                    makeBackup()
                    logger.debug("'$dbName' database: loading empty db...")
                    return empty()
                }
            }
        }
    }

    private fun readFromFile(dbName: String, schemaVersion: Int): T {
        val filename = buildFilename(dbName, schemaVersion)
        val file = File(path, filename).absoluteFile
        if (!file.exists())
            throw FileNotFoundException("file not found: file")
        if (!file.canRead())
            throw RuntimeException("No permission to read a file: $file")
        if (!file.isFile)
            throw RuntimeException("Path is not a regular file: $file")

        try {
            val size = file.length()
            if (size == 0L) {
                throw RuntimeException("File seems to be empty (due to insufficient permissions or corrupted file): $file")
            }
            val bytes: ByteArray = file.readBytes()
            if (bytes.isEmpty()) {
                throw RuntimeException("File seems to have zero bytes (due to insufficient permissions or corrupted file, $size length): $file")
            }
            val content: String = bytes.toString(Charsets.UTF_8)
            return json.decodeFromString(serializer, content)

        } catch (e: FileNotFoundException) {
            if (e.message.orEmpty().contains("(Permission denied)", ignoreCase = true)) {
                throw RuntimeException("Permission denied to read a file: $file", e)
            }
            throw e
        }
    }

    fun read(resetOnError: Boolean) {
        db = readDb(resetOnError)
    }

    open fun save() {
        db?.let {
            saveDb(it)
        }
    }

    private fun saveDb(db: T): File {
        return saveToFile(dbName, schemaVersion, db)
    }

    private fun saveToFile(dbName: String, schemaVersion: Int, obj: T): File {
        json.encodeToJsonElement(serializer, obj)
        val content = json.encodeToString(serializer, obj)
        val filename = buildFilename(dbName, schemaVersion)
        val file = File(path, filename)
        file.writeText(content, Charsets.UTF_8)
        return file
    }

    private fun buildFilename(name: String, schemaVersion: Int): String {
        return "$name.$schemaVersion.json"
    }

    private fun makeBackup() {
        val filename = buildFilename(dbName, schemaVersion)
        val dbFile = File(path, filename)
        val backupFile = File(path, "$filename.bak")

        if (dbFile.exists()) {
            if (backupFile.exists()) {
                logger.warn("removing previous backup file: " + backupFile.absolutePath)
                backupFile.delete()
            }

            dbFile.copyTo(backupFile)

            when (backupFile.exists()) {
                true -> logger.info("json db file ${dbFile.absolutePath} copied to backup ${backupFile.absolutePath}")
                false -> logger.error("failed to copy json db file: " + dbFile.absolutePath)
            }
        }
    }

    fun factoryReset() {
        val filename = buildFilename(dbName, schemaVersion)
        val dbFile = File(path, filename)
        val backupFile = File(path, "$filename.bak")

        if (dbFile.exists()) {
            if (backupFile.exists()) {
                logger.warn("removing previous backup file: " + backupFile.absolutePath)
                backupFile.delete()
            }

            dbFile.renameTo(backupFile)

            when (dbFile.exists()) {
                true -> logger.error("failed to rename json db file: " + dbFile.absolutePath)
                false -> logger.info("json db file ${dbFile.absolutePath} moved to backup ${backupFile.absolutePath}")
            }
        }

        db = empty()
        save()
    }
}
