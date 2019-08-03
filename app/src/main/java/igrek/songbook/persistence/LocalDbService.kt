package igrek.songbook.persistence

import android.annotation.SuppressLint
import android.app.Activity
import android.database.sqlite.SQLiteDatabase
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.system.PermissionService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class LocalDbService {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var permissionService: Lazy<PermissionService>

    private val logger = LoggerFactory.logger
    private var songsDbHelper: SQLiteDatabase? = null

    private val currentSchemaVersion = 2
    private val currentSongsDbFilename = "songs.$currentSchemaVersion.sqlite"

    private val songDbDir: File
        @SuppressLint("SdCardPath")
        get() {
            /*
            1. /data/data/PACKAGE/files
            2. INTERNAL_STORAGE/Android/data/PACKAGE/files/data
            */
            var dir: File? = activity.filesDir
            if (dir != null && dir.isDirectory)
                return dir

            if (permissionService.get().isStoragePermissionGranted) {
                dir = activity.getExternalFilesDir("data")
                if (dir != null && dir.isDirectory)
                    return dir
            }

            return File("/data/data/" + activity.packageName + "/files")
        }

    val songsDbFile: File
        get() = File(songDbDir, currentSongsDbFilename)

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun openSongsDb(): SQLiteDatabase {
        if (songsDbHelper == null) {
            val dbFile = songsDbFile
            // if file does not exist - copy initial db from resources
            if (!dbFile.exists())
                copyFileFromResources(R.raw.songs, dbFile)
            songsDbHelper = openDatabase(dbFile)
        }
        return songsDbHelper!!
    }

    fun closeDatabases() {
        songsDbHelper?.close()
        songsDbHelper = null
    }

    fun factoryReset() {
        // remove db files
        factoryResetSongsDb()
        // need to reopen dbs again (in external dependencies)
    }

    fun factoryResetSongsDb() {
        if (songsDbHelper != null) {
            songsDbHelper!!.close()
            songsDbHelper = null
        }
        removeDb(songsDbFile)
    }

    private fun removeDb(songsDbFile: File) {
        if (songsDbFile.exists()) {
            if (!songsDbFile.delete() || songsDbFile.exists())
                logger.error("failed to delete database file: " + songsDbFile.absolutePath)
        }
    }

    private fun openDatabase(songsDbFile: File): SQLiteDatabase {
        if (!songsDbFile.exists())
            logger.warn("Database file does not exist: " + songsDbFile.absolutePath)
        return SQLiteDatabase.openDatabase(songsDbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)
    }

    private fun copyFileFromResources(resourceId: Int, targetPath: File) {
        val buff = ByteArray(1024)
        try {
            activity.resources.openRawResource(resourceId).use { input ->
                FileOutputStream(targetPath).use { out ->
                    while (true) {
                        val read = input.read(buff)
                        if (read <= 0)
                            break
                        out.write(buff, 0, read)
                    }
                    out.flush()
                }
            }
        } catch (e: IOException) {
            logger.error(e)
        }
    }

}
