package igrek.songbook.persistence.user

import igrek.songbook.info.logger.LoggerFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.io.File
import java.io.FileNotFoundException

abstract class AbstractUserDataService<T>(
        private val path: String,
        val dbName: String,
        val schemaVersion: Int,
        val clazz: Class<T>,
        val serializer: KSerializer<T>
) {

    private val json = Json(JsonConfiguration.Stable)
    private val logger = LoggerFactory.logger

    private fun readFromFile(dbName: String, schemaVersion: Int): T {
        val filename = buildFilename(dbName, schemaVersion)
        val file = File(path, filename)
        if (!file.exists())
            throw FileNotFoundException()

        val content = file.readText(Charsets.UTF_8)
        return json.parse(serializer, content)
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

    fun read(): T {
        return try {
            readFromFile(dbName, schemaVersion)
        } catch (e: FileNotFoundException) {
            logger.error("database file not found for db $dbName")
            empty()
        } catch (e: SerializationException) {
            logger.error("JSON reading error for db $dbName: ${e.message}")
            empty()
        }
    }

    abstract fun empty(): T

    fun save(db: T) {
        saveToFile(dbName, schemaVersion, db)
    }
}