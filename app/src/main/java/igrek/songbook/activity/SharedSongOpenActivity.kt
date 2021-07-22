package igrek.songbook.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.info.logger.LoggerFactory.logger


class SharedSongOpenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data: Uri? = intent?.data
        val encodedSong = data?.path?.removePrefix("/song/")
        logger.info("opening encoded shared song: $encodedSong")

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("encodedSong", encodedSong);
        startActivity(intent)
        finish()
    }

}
