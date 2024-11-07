package igrek.songbook.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.info.logger.LoggerFactory.logger


class TvSplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logger.debug("Starting TvSplashScreenActivity")
        val intent = Intent(applicationContext, TvActivity::class.java)
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        finish()
    }

}
