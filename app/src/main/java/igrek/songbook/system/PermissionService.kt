package igrek.songbook.system

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import javax.inject.Inject

class PermissionService {

    private val logger = LoggerFactory.logger

    @Inject
    lateinit var activity: Activity

    // Permission is granted
    // Permission is revoked
    //permission is automatically granted on sdk<23 upon installation
    // Permission is granted
    val isStoragePermissionGranted: Boolean
        get() {
            return if (Build.VERSION.SDK_INT >= 23) {
                if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    true
                } else {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    false
                }
            } else {
                true
            }
        }

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    private fun onPermissionGranted(permission: String) {
        logger.info("permission $permission has been granted")
    }

    private fun onPermissionDenied(permission: String) {
        logger.warn("permission $permission has been denied")
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permissions[0])
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                onPermissionDenied(permissions[0])
            }
        }
    }
}
