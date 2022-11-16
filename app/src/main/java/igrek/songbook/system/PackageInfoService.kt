package igrek.songbook.system

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

@Suppress("DEPRECATION")
class PackageInfoService(
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
) {
    private val activity by LazyExtractor(appCompatActivity)

    private val logger = LoggerFactory.logger
    var versionName: String? = null
        private set
    var versionCode: Long = 0
        private set

    init {
        try {
            val pInfo = activity.packageManager
                .getPackageInfo(activity.packageName, 0)
            versionName = pInfo.versionName
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                pInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            logger.error(e)
        }
    }
}
