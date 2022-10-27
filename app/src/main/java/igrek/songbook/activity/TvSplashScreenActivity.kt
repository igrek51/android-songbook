package igrek.songbook.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class TvSplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(applicationContext, TvActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

}
