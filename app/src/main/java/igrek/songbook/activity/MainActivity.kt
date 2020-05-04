package igrek.songbook.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.custom.SongExportFileChooser
import igrek.songbook.custom.SongImportFileChooser
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.AppContextFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.settings.sync.GoogleSyncManager
import igrek.songbook.system.PermissionService
import igrek.songbook.system.SystemKeyDispatcher
import igrek.songbook.util.RetryDelayed


open class MainActivity(
        appInitializer: LazyInject<AppInitializer> = appFactory.appInitializer,
        activityController: LazyInject<ActivityController> = appFactory.activityController,
        optionSelectDispatcher: LazyInject<OptionSelectDispatcher> = appFactory.optionSelectDispatcher,
        systemKeyDispatcher: LazyInject<SystemKeyDispatcher> = appFactory.systemKeyDispatcher,
        permissionService: LazyInject<PermissionService> = appFactory.permissionService,
        songImportFileChooser: LazyInject<SongImportFileChooser> = appFactory.songImportFileChooser,
        songExportFileChooser: LazyInject<SongExportFileChooser> = appFactory.songExportFileChooser,
        googleSyncManager: LazyInject<GoogleSyncManager> = appFactory.googleSyncManager,
) : AppCompatActivity() {
    private var appInitializer by LazyExtractor(appInitializer)
    private var activityController by LazyExtractor(activityController)
    private var optionSelectDispatcher by LazyExtractor(optionSelectDispatcher)
    private var systemKeyDispatcher by LazyExtractor(systemKeyDispatcher)
    private var permissionService by LazyExtractor(permissionService)
    private var songImportFileChooser by LazyExtractor(songImportFileChooser)
    private var songExportFileChooser by LazyExtractor(songExportFileChooser)
    private var googleSyncManager by LazyExtractor(googleSyncManager)

    private val logger: Logger = LoggerFactory.logger

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            logger.info("Creating Dependencies container...")
            AppContextFactory.createAppContext(this)
            recreateFields() // Workaround for reusing finished activities by Android
            super.onCreate(savedInstanceState)
            appInitializer.init()
        } catch (t: Throwable) {
            logger.fatal(t)
            throw t
        }
    }

    private fun recreateFields() {
        appInitializer = appFactory.appInitializer.get()
        activityController = appFactory.activityController.get()
        optionSelectDispatcher = appFactory.optionSelectDispatcher.get()
        systemKeyDispatcher = appFactory.systemKeyDispatcher.get()
        permissionService = appFactory.permissionService.get()
        songImportFileChooser = appFactory.songImportFileChooser.get()
        songExportFileChooser = appFactory.songExportFileChooser.get()
        googleSyncManager = appFactory.googleSyncManager.get()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        activityController.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        super.onDestroy()
        activityController.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).post {
            RetryDelayed(10, 500, UninitializedPropertyAccessException::class.java) {
                activityController.onStart()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        activityController.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return optionSelectDispatcher.optionsSelect(item.itemId) || super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (systemKeyDispatcher.onKeyBack())
                    return true
            }
            KeyEvent.KEYCODE_MENU -> {
                if (systemKeyDispatcher.onKeyMenu())
                    return true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (systemKeyDispatcher.onVolumeUp())
                    return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (systemKeyDispatcher.onVolumeDown())
                    return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionService.onRequestPermissionsResult(permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SongImportFileChooser.FILE_SELECT_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    songImportFileChooser.onFileSelect(data?.data)
                }
            SongExportFileChooser.FILE_EXPORT_SELECT_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    songExportFileChooser.onFileSelect(data?.data)
                }
            GoogleSyncManager.REQUEST_CODE_SIGN_IN_SYNC_SAVE,
            GoogleSyncManager.REQUEST_CODE_SIGN_IN_SYNC_RESTORE ->
                googleSyncManager.handleSignInResult(data, this, requestCode, resultCode)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
