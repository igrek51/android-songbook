package igrek.songbook.persistence.user.custom

import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.contextmenu.ContextMenuBuilder
import igrek.songbook.layout.dialog.ConfirmDialogBuilder
import igrek.songbook.persistence.LocalFilesystem
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.system.filesystem.copyFile
import igrek.songbook.util.formatTodayDate
import igrek.songbook.util.parseDate
import java.io.File
import java.util.*

const val BACKUP_FILE_SUFFIX: String = "-customsongs.json"
const val BACKUP_KEEP_LAST_DAYS: Int = 14

class CustomSongsBackuper(
    localFilesystem: LazyInject<LocalFilesystem> = appFactory.localFilesystem,
    userDataDao: LazyInject<UserDataDao> = appFactory.userDataDao,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
) {
    private val localFilesystem by LazyExtractor(localFilesystem)
    private val userDataDao by LazyExtractor(userDataDao)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val preferencesState by LazyExtractor(preferencesState)

    private val logger = LoggerFactory.logger

    private val songsBackupDir: File
        get() {
            val songBackupDir: File = localFilesystem.backupDir.resolve("customsongs")
            if (!songBackupDir.isDirectory)
                songBackupDir.mkdirs()
            return songBackupDir
        }

    fun saveBackup() {
        safeExecute {
            if (!preferencesState.saveCustomSongsBackups)
                return

            val dbFile = getDbFilePath()
            val backupDir: File = songsBackupDir

            val todayStr = formatTodayDate()
            val backupFile: File = backupDir.resolve("${todayStr}$BACKUP_FILE_SUFFIX")

            val existed = backupFile.isFile
            copyFile(dbFile, backupFile)
            if (!existed) {
                logger.debug("Custom songs backup created: $backupFile")
            } else {
                logger.debug("Custom songs backup updated: $backupFile")
            }
            removeOldBackups()
        }
    }

    private fun removeOldBackups() {
        val backups = listBackups()
        if (backups.size <= BACKUP_KEEP_LAST_DAYS)
            return

        val oldBackups = backups.drop(BACKUP_KEEP_LAST_DAYS)
        for (oldBackup in oldBackups) {
            if (oldBackup.file.delete()) {
                logger.debug("Old custom songs backup deleted: ${oldBackup.file.name}")
            }
        }
    }

    private fun listBackups(): List<BackupFile> {
        val backupDir: File = songsBackupDir
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

            backups.add(BackupFile(child, date, dateStr))
        }
        backups.sortByDescending { it.date }
        return backups
    }

    private fun getDbFilePath(): File {
        val dbName = userDataDao.customSongsDao.dbName
        val schemaVersion = userDataDao.customSongsDao.schemaVersion
        val filename = "$dbName.$schemaVersion.json"
        val path = localFilesystem.appFilesDir.absolutePath
        return File(path, filename)
    }

    private fun restoreBackup(backupFile: File) {
        val dbFile = getDbFilePath()
        copyFile(backupFile, dbFile)
        userDataDao.reloadCustomSongs()
        logger.info("Backup $backupFile restored to $dbFile")
        uiInfoService.showInfo(R.string.custom_songs_backup_restored)
    }

    fun showRestoreBackupDialog() {
        val backups = listBackups()
        if (backups.isEmpty()){
            uiInfoService.showInfo(R.string.no_backups_to_restore)
            return
        }

        val actions = backups.map { backup ->
            ContextMenuBuilder.Action(backup.formattedDate) {
                ConfirmDialogBuilder().confirmAction(R.string.confirm_restore_custom_songs_backup) {
                    restoreBackup(backup.file)
                }
            }
        }.toList()
        ContextMenuBuilder().showContextMenu(R.string.custom_songs_restore_backup_choose, actions)
    }
}

class BackupFile(
    val file: File,
    val date: Date,
    val formattedDate: String,
)
