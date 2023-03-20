package igrek.songbook.persistence.user.custom

import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.LocalFilesystem
import igrek.songbook.system.filesystem.copyFile
import igrek.songbook.util.formatTodayDate
import igrek.songbook.util.parseDate
import java.io.File
import java.util.*

const val BACKUP_FILE_SUFFIX: String = "-customsongs.json"
const val BACKUP_KEEP_LAST_DAYS: Int = 14

class CustomSongsBackuper(
    localFilesystem: LazyInject<LocalFilesystem> = appFactory.localFilesystem,
) {
    private val localFilesystem by LazyExtractor(localFilesystem)
    private val logger = LoggerFactory.logger

    private val songsBackupDir: File
        get() {
            val songBackupDir: File = localFilesystem.backupDir.resolve("customsongs")
            if (!songBackupDir.isDirectory)
                songBackupDir.mkdirs()
            return songBackupDir
        }

    fun saveBackup(dbFile: File) {
        safeExecute {
            val backupDir: File = songsBackupDir

            val todayStr = formatTodayDate()
            val backupFile: File = backupDir.resolve("${todayStr}$BACKUP_FILE_SUFFIX")

            copyFile(dbFile, backupFile)
            logger.debug("Custom songs backup created: $backupFile")
            removeOldBackups(backupDir)
        }
    }

    private fun removeOldBackups(backupDir: File) {
        val backups = listBackups(backupDir)
        if (backups.size <= BACKUP_KEEP_LAST_DAYS)
            return

        val oldBackups = backups.drop(BACKUP_KEEP_LAST_DAYS)
        for (oldBackup in oldBackups) {
            if (oldBackup.file.delete()) {
                logger.debug("Old custom songs backup deleted: ${oldBackup.file.name}")
            }
        }
    }

    fun listAllBackups(): List<BackupFile> {
        val backupDir: File = songsBackupDir
        return listBackups(backupDir)
    }

    private fun listBackups(backupDir: File): List<BackupFile> {
        val children: List<File> = backupDir.listFiles()?.toList() ?: emptyList()
        val backups: MutableList<BackupFile> = mutableListOf()
        for (child in children) {

            if (!child.name.endsWith(BACKUP_FILE_SUFFIX))
                continue

            val dateStr = child.name.removeSuffix(BACKUP_FILE_SUFFIX)
            val date: Date? = parseDate(dateStr)
            if (date == null) {
                logger.warn("Invalid date format in file name: $child.name")
                continue
            }

            backups.add(BackupFile(child, date))
        }
        backups.sortByDescending { it.date }
        return backups
    }

    fun restoreBackup(backupFile: File, dbFile: File) {
        copyFile(backupFile, dbFile)
        logger.info("Backup $backupFile restored to $dbFile")
    }

}

class BackupFile(
    val file: File,
    val date: Date,
)
