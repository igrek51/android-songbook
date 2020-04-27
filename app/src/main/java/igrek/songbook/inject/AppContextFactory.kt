package igrek.songbook.inject

import androidx.appcompat.app.AppCompatActivity

var appFactory: AppFactory = AppFactory(AppCompatActivity())

object AppContextFactory {
    fun createAppContext(activity: AppCompatActivity) {
        appFactory = AppFactory(activity)
    }
}
