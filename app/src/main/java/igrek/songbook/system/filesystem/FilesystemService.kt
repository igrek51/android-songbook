package igrek.songbook.system.filesystem

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import java.io.*
import java.nio.charset.Charset
import java.util.*
import javax.inject.Inject

/**
 * Filesystem facade
 */
class FilesystemService {

    private val logger = LoggerFactory.logger

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var externalCardService: ExternalCardService

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun mkdirIfNotExist(path: String): Boolean {
        val f = File(path)
        return !f.exists() && f.mkdirs()
    }

    fun listFilenames(path: String): List<String> {
        val files = listFiles(path)
        val filenames = ArrayList<String>()
        for (file in files) {
            filenames.add(file.name)
        }
        return filenames
    }

    fun listFiles(path: String): List<File> {
        val f = File(path)
        val files = Arrays.asList(*f.listFiles())
        Collections.sort(files) { o1, o2 -> o1.name.compareTo(o2.name) }
        return files
    }

    @Throws(IOException::class)
    private fun openFile(filename: String): ByteArray {
        val f = RandomAccessFile(File(filename), "r")
        val length = f.length().toInt()
        val data = ByteArray(length)
        f.readFully(data)
        f.close()
        return data
    }

    @Throws(IOException::class)
    fun openFileString(filename: String): String {
        val bytes = openFile(filename)
        return String(bytes, Charset.forName("UTF-8"))
    }

    @Throws(IOException::class)
    private fun saveFile(filename: String, data: ByteArray) {
        val file = File(filename)
        createMissingParentDir(file)
        val fos: FileOutputStream
        fos = FileOutputStream(file)
        fos.write(data)
        fos.flush()
        fos.close()
    }

    fun createMissingParentDir(file: File) {
        val parentDir = file.parentFile
        if (!parentDir.exists()) {
            if (parentDir.mkdirs()) {
                logger.debug("missing dir created: $parentDir")
            }
        }
    }

    @Throws(IOException::class)
    fun saveFile(filename: String, str: String) {
        saveFile(filename, str.toByteArray())
    }

    @Throws(IOException::class)
    fun copy(source: File, dest: File) {
        FileInputStream(source).use { `is` ->
            FileOutputStream(dest).use { os ->
                val buffer = ByteArray(1024)
                var length: Int
                while (true) {
                    length = `is`.read(buffer)
                    if (length <= 0) {
                        break
                    }
                    os.write(buffer, 0, length)
                }
            }
        }
    }

    fun ensureAppDataDirExists() {
        val externalSD = File(externalCardService.externalSDPath!!)
        val appDataDir = File(externalSD, "Android/data/" + activity.packageName)
        if (!appDataDir.exists()) {
            // WTF!?? getExternalFilesDir creates dir on SD card but returns Internal storage path
            logger.info(activity.getExternalFilesDir("data")!!.absolutePath)
            if (appDataDir.mkdirs() && appDataDir.exists()) {
                logger.debug("Android/data/package directory has been created")
            } else {
                logger.error("Failed to create Android/data/package directory")
            }
        }
    }

    fun trimEndSlash(str: String): String {
        var str = str
        while (str.endsWith("/"))
            str = str.substring(0, str.length - 1)
        return str
    }
}
