package igrek.songbook.activity

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.info.logger.LoggerFactory.logger


class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logger.debug("Starting SplashScreenActivity")
        val activityClass = when {
            isAndroidTV() -> {
                logger.warn("Detected Android TV started from main Launcher, redirecting to TVActivity")
                TvActivity::class.java
            }
            else -> MainActivity::class.java
        }
        val intent = Intent(applicationContext, activityClass)
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        finish()
    }

    private fun isAndroidTV(): Boolean {
        val uiModeManager = getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        val isTelevision = uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        val hasLeanbackFeature = packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        return isTelevision || hasLeanbackFeature
    }
}
