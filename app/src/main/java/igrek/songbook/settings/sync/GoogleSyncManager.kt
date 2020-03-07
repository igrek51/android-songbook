package igrek.songbook.settings.sync

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.activity.ActivityController
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.LocalDbService
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.system.filesystem.saveInputStreamToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

class GoogleSyncManager {

    @Inject
    lateinit var activity: AppCompatActivity
    @Inject
    lateinit var uiInfoService: UiInfoService
    @Inject
    lateinit var localDbService: Lazy<LocalDbService>
    @Inject
    lateinit var songsRepository: Lazy<SongsRepository>
    @Inject
    lateinit var preferencesService: Lazy<PreferencesService>
    @Inject
    lateinit var activityController: Lazy<ActivityController>

    @Inject
    lateinit var userDataDao: Lazy<UserDataDao>

    private val syncFiles = listOf(
            "files/customsongs.1.json",
            "files/exclusion.1.json",
            "files/favourites.1.json",
            "files/history.1.json",
            "files/playlist.1.json",
            "files/transpose.1.json",
            "files/unlocked.1.json",
            "files/preferences.1.json"
    )

    private val logger = LoggerFactory.logger

    companion object {
        const val REQUEST_CODE_SIGN_IN_SYNC_SAVE = 10
        const val REQUEST_CODE_SIGN_IN_SYNC_RESTORE = 11
    }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun syncSave() {
        logger.debug("making application data Backup in Google Drive")
        requestSingIn(REQUEST_CODE_SIGN_IN_SYNC_SAVE)
    }

    fun syncRestore() {
        logger.debug("restoring application data from Google Drive")
        requestSingIn(REQUEST_CODE_SIGN_IN_SYNC_RESTORE)
    }

    private fun syncSaveSignedIn(driveService: Drive) {
        showSyncProgress(0, syncFiles.size + 1)
        GlobalScope.launch(Dispatchers.IO) {
            userDataDao.get().saveNow()
            preferencesService.get().saveAll()
            runCatching {
                syncFiles.forEachIndexed { index, syncFile ->
                    showSyncProgress(index + 1, syncFiles.size + 1)
                    backupFile(driveService, syncFile)
                }
            }.onFailure { error ->
                uiInfoService.showInfoIndefinite(R.string.settings_sync_save_error, error.message
                        ?: "")
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
                        logger.error(e)
                        errors.add(syncFile)
                    }
                }
            }.onFailure { error ->
                uiInfoService.showInfoIndefinite(R.string.settings_sync_restore_error, error.message
                        ?: "")
            }.onSuccess {
                songsRepository.get().reloadSongsDb()
                preferencesService.get().reload()
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
                    activityController.get().quit()
                }
            }
        }
    }

    private fun showSyncProgress(current: Int, count: Int) {
        val percent = current * 100 / count
        uiInfoService.showInfoIndefinite(R.string.settings_sync_in_progress, percent.toString())
    }

    private fun backupFile(driveService: Drive, syncFile: String) {
        logger.debug("backing up file $syncFile")
        val fileId: String = findOrCreateFile(driveService, syncFile)

        val dataDirPath = localDbService.get().appDataDir.absolutePath
        val localFile = java.io.File(dataDirPath, syncFile)
        if (!localFile.exists())
            throw FileNotFoundException("file not found: ${localFile.absoluteFile}")
        val fileContent = FileContent(null, localFile)

        val metadata = File().setName(syncFile)
        driveService.files().update(fileId, metadata, fileContent).execute()
    }

    private fun restoreFile(driveService: Drive, syncFile: String) {
        logger.debug("restoring file $syncFile")
        val fileId: String = findDriveFile(driveService, syncFile)
                ?: throw FileNotFoundException("file not found on Google Drive: $syncFile")

        val dataDirPath = localDbService.get().appDataDir.absolutePath
        val localFile = java.io.File(dataDirPath, syncFile)

        driveService.files()
                .get(fileId)
                .executeMediaAsInputStream().use { inputStream ->
                    saveInputStreamToFile(inputStream, localFile)
                    logger.debug("file $syncFile restored: ${localFile.readLines()}")
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
            val latestFile = namedFiles.maxBy { file -> file.modifiedTime.value }!!
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

    private fun requestSingIn(requestCode: Int) {
        logger.debug("requesting Google Sign In")
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
                .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)

        // The result of the sign-in Intent is handled in onActivityResult.
        activity.startActivityForResult(client.signInIntent, requestCode)
    }

    fun signOut() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
                .build()
        val client = GoogleSignIn.getClient(activity, signInOptions)

        client.signOut().addOnCompleteListener {
            uiInfoService.showInfo(R.string.sync_singed_out)
        }
    }

    fun handleSignInResult(result: Intent?, activity: AppCompatActivity?, requestCode: Int, resultCode: Int) {
        if (resultCode != Activity.RESULT_OK || result == null) {
            logger.warn("Sign in request failed: result code=$resultCode, result=$result, extras=${result?.extras}")
            uiInfoService.showToast(R.string.operation_cancelled)
            return
        }

        GoogleSignIn.getSignedInAccountFromIntent(result).addOnSuccessListener { googleAccount: GoogleSignInAccount ->
            logger.debug("Signed in as ${googleAccount.email}")
            // Use the authenticated account to sign in to the Drive service.
            val credential = GoogleAccountCredential.usingOAuth2(activity, setOf(DriveScopes.DRIVE_APPDATA))
            credential.selectedAccount = googleAccount.account
            val googleDriveService = Drive.Builder(AndroidHttp.newCompatibleTransport(), GsonFactory(), credential)
                    .setApplicationName("igrek.songbook")
                    .build()

            when (requestCode) {
                REQUEST_CODE_SIGN_IN_SYNC_SAVE -> syncSaveSignedIn(googleDriveService)
                REQUEST_CODE_SIGN_IN_SYNC_RESTORE -> syncRestoreSignedIn(googleDriveService)
            }
        }.addOnFailureListener { exception: Exception? ->
            uiInfoService.showInfoIndefinite(R.string.error_unable_to_sing_in_google)
            logger.error("Unable to sign in to Google account", exception!!)
        }
    }
}