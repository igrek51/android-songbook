package igrek.songbook.persistence

import android.annotation.SuppressLint
import android.app.Activity
import igrek.songbook.R
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LocalFilesystem(
    activity: LazyInject<Activity> = appFactory.activity,
) {
    private val activity by LazyExtractor(activity)

    private val logger = LoggerFactory.logger

    private val currentSchemaVersion = 2
    private val currentSongsDbFilename = "songs.$currentSchemaVersion.sqlite"

    val appFilesDir: File
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

            dir = activity.getExternalFilesDir("data")
            if (dir != null && dir.isDirectory)
                return dir

            return File("/data/data/" + activity.packageName + "/files")
        }

    val appDataDir: File
        @SuppressLint("SdCardPath")
        get() {
            return appFilesDir.parentFile ?: File("/data/data/" + activity.packageName)
        }

    val songsDbFile: File
        get() = File(appFilesDir, currentSongsDbFilename)

    val backupDir: File
        @SuppressLint("SdCardPath")
        get() {
            // INTERNAL_STORAGE/Android/data/PACKAGE/files/backup
            val externalFilesDir: File? = activity.getExternalFilesDir(null)
            if (externalFilesDir != null) {
                if (!externalFilesDir.isDirectory)
                    externalFilesDir.mkdirs()
                val backupDir = externalFilesDir.resolve("backup")
                if (!backupDir.isDirectory)
                    backupDir.mkdirs()
                return backupDir
            }

            // /data/data/PACKAGE/files/backup or /data/user/0/PACKAGE/files/backup
            val filesDir: File? = activity.filesDir
            if (filesDir != null && filesDir.isDirectory) {
                val backupDir = filesDir.resolve("backup")
                if (!backupDir.isDirectory)
                    backupDir.mkdirs()
                return backupDir
            }

            // /data/data/PACKAGE/files/backup
            val backupDir = File("/data/data/" + activity.packageName + "/files/backup")
            if (!backupDir.isDirectory)
                backupDir.mkdirs()
            return backupDir
        }

    fun ensureLocalDbExists() {
        val dbFile = songsDbFile
        // if file does not exist - copy initial db from resources
        if (!dbFile.exists())
            copyFileFromResources(R.raw.songs, dbFile)
    }

    fun factoryReset() {
        removeFile(songsDbFile, keepBackup = true)
        removeFile(File(songsDbFile.absolutePath + "-shm"))
        removeFile(File(songsDbFile.absolutePath + "-wal"))
    }

    private fun removeFile(songsDbFile: File, keepBackup: Boolean = false) {
        if (songsDbFile.exists()) {
            when (keepBackup) {
                true -> {
                    val bakFile = File(songsDbFile.absolutePath + ".bak")
                    if (bakFile.exists()) {
                        logger.warn("removing previous backup file: " + bakFile.absolutePath)
                        bakFile.delete()
                    }
                    songsDbFile.renameTo(bakFile)
                    when (songsDbFile.exists()) {
                        true -> logger.error("failed to rename file: " + songsDbFile.absolutePath)
                        false -> logger.info("file ${songsDbFile.absolutePath} moved to backup at ${bakFile.absolutePath}")
                    }
                }
                false -> {
                    if (!songsDbFile.delete() || songsDbFile.exists()) {
                        logger.error("failed to delete file: " + songsDbFile.absolutePath)
                    } else {
                        logger.info("file ${songsDbFile.absolutePath} deleted")
                    }
                }
            }
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
