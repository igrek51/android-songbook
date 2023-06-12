package igrek.songbook.settings.sync

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.activity.ActivityResultDispatcher
import igrek.songbook.custom.ExportFileChooser
import igrek.songbook.custom.ImportFileChooser
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.LocalFilesystem
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.system.filesystem.saveInputStreamToFile
import igrek.songbook.util.formatTimestampDate
import igrek.songbook.util.formatTimestampTime
import igrek.songbook.util.formatTodayDate
import kotlinx.coroutines.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*


class BackupSyncManager(
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    localFilesystem: LazyInject<LocalFilesystem> = appFactory.localFilesystem,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    preferencesService: LazyInject<PreferencesService> = appFactory.preferencesService,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    userDataDao: LazyInject<UserDataDao> = appFactory.userDataDao,
    activityResultDispatcher: LazyInject<ActivityResultDispatcher> = appFactory.activityResultDispatcher,
    exportFileChooser: LazyInject<ExportFileChooser> = appFactory.exportFileChooser,
    importFileChooser: LazyInject<ImportFileChooser> = appFactory.importFileChooser,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
) {
    private val activity by LazyExtractor(appCompatActivity)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val localDbService by LazyExtractor(localFilesystem)
    private val songsRepository by LazyExtractor(songsRepository)
    private val preferencesService by LazyExtractor(preferencesService)
    private val activityController by LazyExtractor(activityController)
    private val userDataDao by LazyExtractor(userDataDao)
    private val activityResultDispatcher by LazyExtractor(activityResultDispatcher)
    private val exportFileChooser by LazyExtractor(exportFileChooser)
    private val importFileChooser by LazyExtractor(importFileChooser)
    private val preferencesState by LazyExtractor(preferencesState)

    private val oldSyncFiles = listOf(
        "files/customsongs.1.json",
        "files/exclusion.2.json",
        "files/favourites.1.json",
        "files/history.1.json",
        "files/playlist.1.json",
        "files/transpose.1.json",
        "files/unlocked.1.json",
        "files/preferences.1.json",
    )
    private val compositeBackupFile: String = "songbook-backup.bak"

    private val logger = LoggerFactory.logger

    fun makeDriveBackupUI(logout: Boolean = true) {
        logger.info("making application data Backup in Google Drive")
        requestSingIn(logout) { driveService: Drive ->
            showSyncProgress(0, 2)
            GlobalScope.launch(Dispatchers.IO) {
                runCatching {
                    preferencesService.dumpAll()
                    userDataDao.saveNow()
                    showSyncProgress(1, 2)
                    makeCompositeDriveBackup(driveService)
                }.onFailure { error ->
                    UiErrorHandler().handleError(error, R.string.settings_sync_save_error)
                }.onSuccess {
                    uiInfoService.showInfo(R.string.settings_sync_save_success)
                }
            }
        }
    }

    fun restoreDriveBackupUI() {
        logger.debug("restoring application data from Google Drive")
        requestSingIn(logout = true) { driveService: Drive ->
            GlobalScope.launch(Dispatchers.IO) {
                runCatching {
                    when {
                        findDriveFile(driveService, compositeBackupFile) != null -> {
                            restoreCompositeDriveBackupUI(driveService)
                        }
                        else -> restoreOldDriveBackupUI(driveService)
                    }
                }.onFailure { error ->
                    UiErrorHandler().handleError(error, R.string.settings_sync_restore_error)
                }
            }
        }
    }

    private suspend fun restoreCompositeDriveBackupUI(driveService: Drive) {
        showSyncProgress(0, 2)
        runCatching {
            showSyncProgress(1, 2)
            restoreCompositeDriveBackup(driveService)
        }.onFailure { error ->
            UiErrorHandler().handleError(error, R.string.settings_sync_restore_error)
        }.onSuccess {
            if (!userDataDao.loadOrExit())
                return
            songsRepository.reloadSongsDb()
            preferencesService.reload()
            uiInfoService.showToast(R.string.settings_sync_restore_success)
            uiInfoService.showInfo(R.string.settings_sync_restore_success)
            withContext(Dispatchers.Main) {
                activityController.quit()
            }
        }
    }

    private suspend fun restoreOldDriveBackupUI(driveService: Drive) {
        showSyncProgress(0, oldSyncFiles.size + 1)
        val errors = mutableListOf<String>()
        runCatching {
            oldSyncFiles.forEachIndexed { index, syncFile ->
                showSyncProgress(index + 1, oldSyncFiles.size + 1)
                try {
                    restoreOldFile(driveService, syncFile)
                } catch (e: FileNotFoundException) {
                    logger.warn(e.message)
                    errors.add(syncFile)
                }
            }
        }.onFailure { error ->
            UiErrorHandler().handleError(error, R.string.settings_sync_restore_error)
        }.onSuccess {
            if (!userDataDao.loadOrExit())
                return
            songsRepository.reloadSongsDb()
            preferencesService.reload()
            if (errors.size == oldSyncFiles.size) {
                uiInfoService.showInfo(R.string.settings_sync_restore_failed)
                return@onSuccess
            }
            if (errors.isEmpty()) {
                uiInfoService.showToast(R.string.settings_sync_restore_success)
                uiInfoService.showInfo(R.string.settings_sync_restore_success)
            } else {
                uiInfoService.showToast(R.string.settings_sync_restore_partial_success)
                uiInfoService.showInfo(R.string.settings_sync_restore_partial_success)
            }
            withContext(Dispatchers.Main) {
                activityController.quit()
            }
        }
    }

    private fun showSyncProgress(current: Int, count: Int) {
        val percent = current * 100 / count
        uiInfoService.showInfo(
            R.string.settings_sync_in_progress,
            percent.toString(),
            indefinite = true
        )
    }

    private fun restoreOldFile(driveService: Drive, syncFile: String) {
        logger.debug("restoring file $syncFile")
        val fileId: String = findDriveFile(driveService, syncFile)
            ?: throw FileNotFoundException("file not found on Google Drive: $syncFile")

        val dataDirPath = localDbService.appDataDir.absolutePath
        val localFile = File(dataDirPath, syncFile)

        driveService.files()
            .get(fileId)
            .executeMediaAsInputStream().use { inputStream ->
                saveInputStreamToFile(inputStream, localFile)
                logger.info("file $syncFile ($fileId) restored: ${localFile.readLines()}")
            }
    }

    private fun makeCompositeDriveBackup(driveService: Drive) {
        val encodedData = BackupEncoder().makeCompositeBackup()
        val cacheFile = File.createTempFile("songbook-backup-", ".bak", activity.cacheDir)
        cacheFile.writeBytes(encodedData.toByteArray())

        val fileContent = FileContent(null, cacheFile)
        val metadata = com.google.api.services.drive.model.File().setName(compositeBackupFile)
        val fileId: String = findOrCreateDriveFile(driveService, compositeBackupFile)
        driveService.files().update(fileId, metadata, fileContent).execute()
        cacheFile.delete()

        preferencesState.lastDriveBackupTimestamp = Date().time / 1000
        logger.info("application data backed up in a composite backup $compositeBackupFile ($fileId) on Google Drive")
    }

    private fun restoreCompositeDriveBackup(driveService: Drive) {
        val fileId: String = findDriveFile(driveService, compositeBackupFile)
            ?: throw FileNotFoundException("file not found on Google Drive: $compositeBackupFile")

        val cacheFile = File.createTempFile("songbook-backup-", ".bak", activity.cacheDir)

        driveService.files()
            .get(fileId)
            .executeMediaAsInputStream().use { inputStream ->
                saveInputStreamToFile(inputStream, cacheFile)
            }

        val encodedData = String(cacheFile.readBytes())
        BackupEncoder().restoreCompositeBackup(encodedData)
        cacheFile.delete()
        logger.info("application data restored from a composite backup")
    }

    private fun findOrCreateDriveFile(driveService: Drive, filename: String): String {
        val existingFileId = findDriveFile(driveService, filename)
        if (existingFileId != null) {
            return existingFileId
        }
        logger.debug("application data file $filename not found - creating new")
        return createDriveFile(driveService, filename)
    }

    private fun findDriveFile(driveService: Drive, filename: String): String? {
        val allFileList: FileList = driveService.files().list().setSpaces("appDataFolder").execute()
        val namedFiles = allFileList.files.filter { file -> file.name == filename }
        if (namedFiles.size == 1) {
            return namedFiles[0].id
        } else if (namedFiles.size > 1) {
            val latestFile = namedFiles.maxByOrNull { file -> file.modifiedTime.value }!!
            logger.warn("found ${namedFiles.size} files named $filename on Drive - getting latest ${latestFile.modifiedTime.value}")
            return latestFile.id
        }
        return null
    }

    private fun createDriveFile(driveService: Drive, filename: String): String {
        val metadata = com.google.api.services.drive.model.File().setParents(listOf("appDataFolder")).setName(filename)

        val googleFile: com.google.api.services.drive.model.File = driveService.files().create(metadata)
            .setFields("id")
            .execute()
            ?: throw IOException("unable to create google drive file.")
        return googleFile.id
    }

    private fun requestSingIn(logout: Boolean, onSignIn: (driveService: Drive) -> Unit) {
        logger.debug("requesting Google Sign In")
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
            .build()
        val client: GoogleSignInClient = GoogleSignIn.getClient(activity, signInOptions)

        when (logout) {
            true -> client.signOut().addOnCompleteListener {
                val signInIntent = client.signInIntent
                if (signInIntent.action == null) {
                    logger.warn("Google SignIn intent action is null")
                    signInIntent.action = "com.google.android.gms.auth.GOOGLE_SIGN_IN"
                }
                if (!isIntentCallable(signInIntent)) {
                    logger.warn("Intent is not callable: $signInIntent")
                }
                // The result of the sign-in Intent is handled in onActivityResult.
                activityResultDispatcher.startActivityForResult(signInIntent) { resultCode: Int, data: Intent? ->
                    handleSignInResult(data, resultCode, onSignIn)
                }
            }
            false -> {
                val signInIntent = client.signInIntent
                if (signInIntent.action == null) {
                    logger.warn("Google SignIn intent action is null")
                    signInIntent.action = "com.google.android.gms.auth.GOOGLE_SIGN_IN"
                }
                if (!isIntentCallable(signInIntent)) {
                    logger.warn("Intent is not callable: $signInIntent")
                }
                // The result of the sign-in Intent is handled in onActivityResult.
                activityResultDispatcher.startActivityForResult(signInIntent) { resultCode: Int, data: Intent? ->
                    handleSignInResult(data, resultCode, onSignIn)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun isIntentCallable(intent: Intent): Boolean {
        val list: List<ResolveInfo> = activity.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )
        return list.isNotEmpty()
    }

    private fun handleSignInResult(
        resultData: Intent?,
        resultCode: Int,
        onSignIn: (driveService: Drive) -> Unit
    ) {
        if (resultCode != Activity.RESULT_OK || resultData == null) {
            logger.warn("Sign in request failed: result code=$resultCode, result=$resultData, extras=${resultData?.extras}")
            resultData?.extras?.keySet()?.forEach { key ->
                @Suppress("DEPRECATION")
                logger.warn("key=$key, value=${resultData.extras?.get(key)}")
            }
            uiInfoService.showToast(R.string.operation_cancelled)
            return
        }

        GoogleSignIn.getSignedInAccountFromIntent(resultData)
            .addOnSuccessListener { googleAccount: GoogleSignInAccount ->
                logger.debug("Signed in as ${googleAccount.email}")
                // Use the authenticated account to sign in to the Drive service.
                val credential =
                    GoogleAccountCredential.usingOAuth2(activity, setOf(DriveScopes.DRIVE_APPDATA))
                credential.selectedAccount = googleAccount.account
                val googleDriveService =
                    Drive.Builder(NetHttpTransport(), GsonFactory(), credential)
                        .setApplicationName("igrek.songbook")
                        .build()

                onSignIn(googleDriveService)
            }.addOnFailureListener { exception: Exception? ->
            uiInfoService.showInfo(R.string.error_unable_to_sing_in_google, indefinite = true)
            logger.error("Unable to sign in to Google account", exception!!)
        }
    }

    fun signOut() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
            .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)

        client.signOut().addOnCompleteListener {
            uiInfoService.showInfo(R.string.sync_singed_out)
        }
    }

    fun makeCompositeFileBackupUI() {
        val today = formatTodayDate()
        val filename = "songbook-backup-$today.bak"
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                val encodedData: String = BackupEncoder().makeCompositeBackup()
                withContext(Dispatchers.Main) {
                    exportFileChooser.showFileChooser(encodedData, filename) {
                        uiInfoService.showInfo(R.string.backup_file_exported)
                    }
                }
            }.onFailure { error ->
                UiErrorHandler().handleError(error, R.string.settings_sync_save_error)
            }
        }
    }

    fun restoreCompositeFileBackupUI() {
        importFileChooser.importFile(sizeLimit = 100 * 1024 * 1024) { content: String, filename: String ->
            logger.info("Restoring backup data from a file $filename")
            GlobalScope.launch(Dispatchers.IO) {
                runCatching {
                    BackupEncoder().restoreCompositeBackup(content)
                }.onFailure { error ->
                    UiErrorHandler().handleError(error, R.string.settings_sync_restore_error)
                }.onSuccess {
                    if (!userDataDao.loadOrExit())
                        return@launch
                    songsRepository.reloadSongsDb()
                    preferencesService.reload()
                    uiInfoService.showToast(R.string.settings_sync_restore_success)
                    uiInfoService.showInfo(R.string.settings_sync_restore_success)
                    withContext(Dispatchers.Main) {
                        activityController.quit()
                    }
                }
            }
        }
    }

    fun needsAutomaticBackup(): Boolean {
        if (!preferencesState.syncBackupAutomatically)
            return false
        if (preferencesState.lastDriveBackupTimestamp == 0L)
            return true

        val lastDate = formatTimestampDate(preferencesState.lastDriveBackupTimestamp)
        val todayDate = formatTodayDate()
        return lastDate != todayDate
    }

    fun makeAutomaticBackup() {
        logger.debug("making automatic Backup")
        GlobalScope.launch(Dispatchers.Main) {
            runCatching {
                makeDriveBackupUI(logout = false)
            }.onFailure { error ->
                UiErrorHandler().handleError(error, R.string.settings_sync_save_error)
            }
        }
    }

    fun formatLastBackupTime(): String {
        if (preferencesState.lastDriveBackupTimestamp == 0L)
            return uiInfoService.resString(R.string.none)
        return formatTimestampTime(preferencesState.lastDriveBackupTimestamp)
    }
}
