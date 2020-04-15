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
import dagger.Lazy
import igrek.songbook.custom.SongExportFileChooser
import igrek.songbook.custom.SongImportFileChooser
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.settings.sync.GoogleSyncManager
import igrek.songbook.system.PermissionService
import igrek.songbook.system.SystemKeyDispatcher
import igrek.songbook.util.RetryDelayed
import javax.inject.Inject


open class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appInitializer: Lazy<AppInitializer>
    @Inject
    lateinit var activityController: Lazy<ActivityController>
    @Inject
    lateinit var optionSelectDispatcher: Lazy<OptionSelectDispatcher>
    @Inject
    lateinit var systemKeyDispatcher: Lazy<SystemKeyDispatcher>
    @Inject
    lateinit var permissionService: Lazy<PermissionService>
    @Inject
    lateinit var songImportFileChooser: Lazy<SongImportFileChooser>

    @Inject
    lateinit var songExportFileChooser: Lazy<SongExportFileChooser>
    @Inject
    lateinit var googleSyncManager: Lazy<GoogleSyncManager>

    private val logger: Logger = LoggerFactory.logger

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            // Dagger Container init
            DaggerIoc.init(this)
            super.onCreate(savedInstanceState)
            DaggerIoc.factoryComponent.inject(this)
            appInitializer.get().init()
        } catch (t: Throwable) {
            logger.fatal(t)
            throw t
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        activityController.get().onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        super.onDestroy()
        activityController.get().onDestroy()
    }

    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).post {
            RetryDelayed(10, 500, UninitializedPropertyAccessException::class.java) {
                activityController.get().onStart()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        activityController.get().onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return optionSelectDispatcher.get().optionsSelect(item.itemId) || super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (systemKeyDispatcher.get().onKeyBack())
                    return true
            }
            KeyEvent.KEYCODE_MENU -> {
                if (systemKeyDispatcher.get().onKeyMenu())
                    return true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (systemKeyDispatcher.get().onVolumeUp())
                    return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (systemKeyDispatcher.get().onVolumeDown())
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
        permissionService.get().onRequestPermissionsResult(permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SongImportFileChooser.FILE_SELECT_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    songImportFileChooser.get().onFileSelect(data?.data)
                }
            SongExportFileChooser.FILE_EXPORT_SELECT_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    songExportFileChooser.get().onFileSelect(data?.data)
                }
            GoogleSyncManager.REQUEST_CODE_SIGN_IN_SYNC_SAVE,
            GoogleSyncManager.REQUEST_CODE_SIGN_IN_SYNC_RESTORE ->
                googleSyncManager.get().handleSignInResult(data, this, requestCode, resultCode)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
