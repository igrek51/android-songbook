package igrek.songbook.system

import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import javax.inject.Inject

class PackageInfoService {

    @Inject
    lateinit var activity: AppCompatActivity

    private val logger = LoggerFactory.logger
    var versionName: String? = null
        private set
    var versionCode: Int = 0
        private set

    init {
        DaggerIoc.factoryComponent.inject(this)

        try {
            val pInfo = activity.packageManager
                    .getPackageInfo(activity.packageName, 0)
            versionName = pInfo.versionName
            versionCode = pInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            logger.error(e)
        }

    }
}
