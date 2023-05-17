package igrek.songbook.settings.sync

import igrek.songbook.info.errorcheck.ContextError
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.LocalFilesystem
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.commons.codec.binary.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class BackupEncoder(
    localFilesystem: LazyInject<LocalFilesystem> = appFactory.localFilesystem,
) {
    private val localDbService by LazyExtractor(localFilesystem)

    private val encryptionAlgorithm = "AES/CBC/PKCS5Padding"
    private val encryptionKey: SecretKeySpec = SecretKeySpec("n75V9&bB4ufzR^fQ".toByteArray(), "AES") // Public shared key
    private val encryptionIv = IvParameterSpec("sW637CJJ&rzy8RPA".toByteArray()) // Initialization Vector

    private val jsonSerializer = Json {
        encodeDefaults = true
        ignoreUnknownKeys = false
        isLenient = false
        allowStructuredMapKeys = true
        prettyPrint = false
        useArrayPolymorphism = false
    }

    fun makeCompositeBackup(): String {
        val compositeBackup = CompositeBackup(
            backupVersion = 2,
            customsongs = encodeFileBackup("customsongs.1.json"),
            exclusion = encodeFileBackup("exclusion.2.json"),
            favourites = encodeFileBackup("favourites.1.json"),
            history = encodeFileBackup("history.1.json"),
            playlist = encodeFileBackup("playlist.1.json"),
            transpose = encodeFileBackup("transpose.1.json"),
            unlocked = encodeFileBackup("unlocked.1.json"),
            preferences = encodeFileBackup("preferences.1.json"),
        )
        try {
            return jsonSerializer.encodeToString(CompositeBackup.serializer(), compositeBackup)
        } catch (t: Throwable) {
            throw ContextError("Serializing to JSON", t)
        }
    }

    fun restoreCompositeBackup(data: String) {
        try{
            val compositeBackup: CompositeBackup = jsonSerializer.decodeFromString(CompositeBackup.serializer(), data)

            restoreFileBackup("customsongs.1.json", compositeBackup.customsongs)
            restoreFileBackup("exclusion.2.json", compositeBackup.exclusion)
            restoreFileBackup("favourites.1.json", compositeBackup.favourites)
            restoreFileBackup("history.1.json", compositeBackup.history)
            restoreFileBackup("playlist.1.json", compositeBackup.playlist)
            restoreFileBackup("transpose.1.json", compositeBackup.transpose)
            restoreFileBackup("unlocked.1.json", compositeBackup.unlocked)
            restoreFileBackup("preferences.1.json", compositeBackup.preferences)
        } catch (t: Throwable) {
            throw ContextError("Invalid backup file data", t)
        }
    }

    private fun encodeFileBackup(filename: String): String {
        val fileContent: ByteArray = readAppDataFileContent(filename)
        val zipped: ByteArray = gzip(fileContent)
        val encrypted: ByteArray = aesEncrypt(zipped)
        return base64Encode(encrypted)
    }

    private fun restoreFileBackup(filename: String, data: String?) {
        if (data == null)
            return
        val fileContent: ByteArray = decodeFileBackup(data)
        try {
            writeAppDataFileContent(filename, fileContent)
        } catch (t: Throwable) {
            throw ContextError("Restoring app data file $filename", t)
        }
    }

    fun decodeFileBackup(data: String): ByteArray {
        try{
            val encrypted: ByteArray = base64Decode(data)
            val zipped: ByteArray = aesDecrypt(encrypted)
            return ungzip(zipped)
        } catch (t: Throwable) {
            throw ContextError("Failed to decode backup data", t)
        }
    }

    private fun readAppDataFileContent(filename: String): ByteArray {
        val appFilesDir = File(localDbService.appDataDir.absolutePath, "files")
        val localFile = File(appFilesDir, filename)
        if (!localFile.exists())
            throw FileNotFoundException("file not found: ${localFile.absoluteFile}")
        return localFile.readBytes()
    }

    private fun writeAppDataFileContent(filename: String, content: ByteArray) {
        val appFilesDir = File(localDbService.appDataDir.absolutePath, "files")
        val localFile = File(appFilesDir, filename)
        val bakFile = File(appFilesDir, "$filename.bak")

        if (localFile.exists()) {
            if (bakFile.exists()) {
                logger.warn("removing previous backup file: " + bakFile.absolutePath)
                bakFile.delete()
            }

            localFile.renameTo(bakFile)

            if (localFile.exists()) {
                logger.error("failed to rename file: " + localFile.absolutePath)
            }

            localFile.writeBytes(content)
            logger.info("file ${localFile.absolutePath} restored, old file backed up at ${bakFile.absolutePath}")

        } else {
            localFile.writeBytes(content)
            logger.info("file ${localFile.absolutePath} restored")
        }
    }

    fun aesEncrypt(inputBytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(encryptionAlgorithm)
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, encryptionIv)
        return cipher.doFinal(inputBytes)
    }

    fun aesDecrypt(inputBytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(encryptionAlgorithm)
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, encryptionIv)
        return cipher.doFinal(inputBytes)
    }

}

@Serializable
data class CompositeBackup(
    val backupVersion: Long,

    val customsongs: String?,
    val exclusion: String?,
    val favourites: String?,
    val history: String?,
    val playlist: String?,
    val transpose: String?,
    val unlocked: String?,
    val preferences: String?,
)

fun gzip(bytes: ByteArray): ByteArray {
    val byteStream = ByteArrayOutputStream()
    val zipStream = GZIPOutputStream(byteStream)
    zipStream.write(bytes)
    zipStream.close()
    byteStream.close()
    return byteStream.toByteArray()
}

fun ungzip(content: ByteArray): ByteArray {
    val byteStream = content.inputStream()
    val zipStream = GZIPInputStream(byteStream)
    val bytes = zipStream.readBytes()
    zipStream.close()
    byteStream.close()
    return bytes
}

fun base64Encode(bytes: ByteArray): String {
    return String(Base64.encodeBase64(bytes))
}

fun base64Decode(str: String): ByteArray {
    return Base64.decodeBase64(str)
}