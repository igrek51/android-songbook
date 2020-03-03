package igrek.songbook.system

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import javax.inject.Inject

class PackageInfoService {

    @Inject
    lateinit var activity: AppCompatActivity

    private val logger = LoggerFactory.logger
    var versionName: String? = null
        private set
    var versionCode: Long = 0
        private set

    init {
        DaggerIoc.factoryComponent.inject(this)

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
