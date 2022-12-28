package igrek.songbook.settings.sync

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.activity.ActivityResultDispatcher
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.system.filesystem.saveInputStreamToFile
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.io.FileNotFoundException
import java.io.IOException


@OptIn(DelicateCoroutinesApi::class)
class BackupSyncManager(
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    localDbService: LazyInject<LocalDbService> = appFactory.localDbService,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    preferencesService: LazyInject<PreferencesService> = appFactory.preferencesService,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
    userDataDao: LazyInject<UserDataDao> = appFactory.userDataDao,
    activityResultDispatcher: LazyInject<ActivityResultDispatcher> = appFactory.activityResultDispatcher,
) {
    private val activity by LazyExtractor(appCompatActivity)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val localDbService by LazyExtractor(localDbService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val preferencesService by LazyExtractor(preferencesService)
    private val activityController by LazyExtractor(activityController)
    private val userDataDao by LazyExtractor(userDataDao)
    private val activityResultDispatcher by LazyExtractor(activityResultDispatcher)

    private val syncFiles = listOf(
        "files/customsongs.1.json",
        "files/exclusion.2.json",
        "files/favourites.1.json",
        "files/history.1.json",
        "files/playlist.1.json",
        "files/transpose.1.json",
        "files/unlocked.1.json",
        "files/preferences.1.json",
    )

    @Serializable
    data class CompositeBackup(
        val customsongs: String,
        val exclusion: String,
        val favourites: String,
        val history: String,
        val playlist: String,
        val transpose: String,
        val unlocked: String,
        val preferences: String,
    )

    private val compositeBackupFile: String = "songbook-backup.bak"

    private val logger = LoggerFactory.logger

    fun syncSave() {
        logger.debug("making application data Backup in Google Drive")
        requestSingIn(::syncSaveSignedIn)
    }

    fun syncRestore() {
        logger.debug("restoring application data from Google Drive")
        requestSingIn(::syncRestoreSignedIn)
    }

    private fun syncSaveSignedIn(driveService: Drive) {
        showSyncProgress(0, syncFiles.size + 1)
        GlobalScope.launch(Dispatchers.IO) {
            userDataDao.saveNow()
            preferencesService.saveAll()
            runCatching {
                syncFiles.forEachIndexed { index, syncFile ->
                    showSyncProgress(index + 1, syncFiles.size + 1)
                    backupFile(driveService, syncFile)
                }
            }.onFailure { error ->
                logger.error(error)
                uiInfoService.showInfo(
                    R.string.settings_sync_save_error,
                    error.message.orEmpty(),
                    indefinite = true
                )
            }.onSuccess {
                uiInfoService.showInfo(R.string.settings_sync_save_success)
            }
        }
    }

    private fun syncRestoreSignedIn(driveService: Drive) {
        showSyncProgress(0, syncFiles.size + 1)
        GlobalScope.launch(Dispatchers.IO) {
            val errors = mutableListOf<String>()
            runCatching {
                syncFiles.forEachIndexed { index, syncFile ->
                    showSyncProgress(index + 1, syncFiles.size + 1)
                    try {
                        restoreFile(driveService, syncFile)
                    } catch (e: FileNotFoundException) {
                        logger.warn(e.message)
                        errors.add(syncFile)
                    }
                }
            }.onFailure { error ->
                logger.error(error)
                uiInfoService.showInfo(
                    R.string.settings_sync_restore_error,
                    error.message.orEmpty(),
                    indefinite = true
                )
            }.onSuccess {
                songsRepository.reloadSongsDb()
                preferencesService.reload()
                if (errors.size == syncFiles.size) {
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
    }

    private fun showSyncProgress(current: Int, count: Int) {
        val percent = current * 100 / count
        uiInfoService.showInfo(
            R.string.settings_sync_in_progress,
            percent.toString(),
            indefinite = true
        )
    }

    private fun backupFile(driveService: Drive, syncFile: String) {
        logger.debug("backing up file $syncFile")
        val fileId: String = findOrCreateFile(driveService, syncFile)

        val dataDirPath = localDbService.appDataDir.absolutePath
        val localFile = java.io.File(dataDirPath, syncFile)
        if (!localFile.exists())
            throw FileNotFoundException("file not found: ${localFile.absoluteFile}")
        val fileContent = FileContent(null, localFile)

        val metadata = File().setName(syncFile)
        driveService.files().update(fileId, metadata, fileContent).execute()
        logger.info("file $syncFile ($fileId) backed up: ${localFile.readLines()}")
    }

    private fun restoreFile(driveService: Drive, syncFile: String) {
        logger.debug("restoring file $syncFile")
        val fileId: String = findDriveFile(driveService, syncFile)
            ?: throw FileNotFoundException("file not found on Google Drive: $syncFile")

        val dataDirPath = localDbService.appDataDir.absolutePath
        val localFile = java.io.File(dataDirPath, syncFile)

        driveService.files()
            .get(fileId)
            .executeMediaAsInputStream().use { inputStream ->
                saveInputStreamToFile(inputStream, localFile)
                logger.info("file $syncFile ($fileId) restored: ${localFile.readLines()}")
            }
    }

    private fun findOrCreateFile(driveService: Drive, filename: String): String {
        val existingFileId = findDriveFile(driveService, filename)
        if (existingFileId != null) {
            return existingFileId
        }
        logger.debug("application data file $filename not found - creating new")
        return createFile(driveService, filename)
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

    private fun createFile(driveService: Drive, filename: String): String {
        val metadata = File().setParents(listOf("appDataFolder")).setName(filename)

        val googleFile: File = driveService.files().create(metadata)
            .setFields("id")
            .execute()
            ?: throw IOException("unable to create google drive file.")
        return googleFile.id
    }

    private fun requestSingIn(onSignIn: (driveService: Drive) -> Unit) {
        logger.debug("requesting Google Sign In")
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
            .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)

        client.signOut().addOnCompleteListener {
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

    @Suppress("DEPRECATION")
    private fun isIntentCallable(intent: Intent): Boolean {
        val list: List<ResolveInfo> = activity.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )
        return list.isNotEmpty()
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
}