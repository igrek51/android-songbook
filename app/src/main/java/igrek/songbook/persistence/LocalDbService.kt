package igrek.songbook.persistence

import android.annotation.SuppressLint
import android.app.Activity
import igrek.songbook.R
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.system.PermissionService
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LocalDbService(
        activity: LazyInject<Activity> = appFactory.activity,
        permissionService: LazyInject<PermissionService> = appFactory.permissionService,
) {
    private val activity by LazyExtractor(activity)
    private val permissionService by LazyExtractor(permissionService)

    private val logger = LoggerFactory.logger

    private val currentSchemaVersion = 2
    private val currentSongsDbFilename = "songs.$currentSchemaVersion.sqlite"

    val songDbDir: File
        @SuppressLint("SdCardPath")
        get() {
            /*
            1. /data/data/PACKAGE/files or /data/user/0/PACKAGE/files
            2. INTERNAL_STORAGE/Android/data/PACKAGE/files/data
            3. /data/data/PACKAGE/files
            */
            var dir: File? = activity.filesDir
            if (dir != null && dir.isDirectory)
                return dir

            if (permissionService.isStoragePermissionGranted) {
                dir = activity.getExternalFilesDir("data")
                if (dir != null && dir.isDirectory)
                    return dir
            }

            return File("/data/data/" + activity.packageName + "/files")
        }

    val appDataDir: File
        @SuppressLint("SdCardPath")
        get() {
            return songDbDir.parentFile
        }

    val songsDbFile: File
        get() = File(songDbDir, currentSongsDbFilename)

    fun ensureLocalDbExists() {
        val dbFile = songsDbFile
        // if file does not exist - copy initial db from resources
        if (!dbFile.exists())
            copyFileFromResources(R.raw.songs, dbFile)
    }

    fun factoryReset() {
        removeFile(songsDbFile)
        removeFile(File(songsDbFile.absolutePath + "-shm"))
        removeFile(File(songsDbFile.absolutePath + "-wal"))
    }

    private fun removeFile(songsDbFile: File) {
        if (songsDbFile.exists()) {
            if (!songsDbFile.delete() || songsDbFile.exists())
                logger.error("failed to delete file: " + songsDbFile.absolutePath)
        }
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
