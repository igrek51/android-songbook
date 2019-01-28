package igrek.songbook.activity

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.MenuItem
import dagger.Lazy
import igrek.songbook.custom.SongImportFileChooser
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.system.PermissionService
import igrek.songbook.system.SystemKeyDispatcher
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

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

    private val logger: Logger = LoggerFactory.logger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // Dagger Container init
            DaggerIoc.init(this)
            DaggerIoc.getFactoryComponent().inject(this)
            appInitializer.get().init()
        } catch (t: Throwable) {
            logger.fatal(this, t)
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
        activityController.get().onStart()
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
        permissionService.get().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SongImportFileChooser.FILE_SELECT_CODE -> if (resultCode == Activity.RESULT_OK) {
                songImportFileChooser.get().onFileSelect(data?.data)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
