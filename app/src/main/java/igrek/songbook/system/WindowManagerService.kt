package igrek.songbook.system


import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

class WindowManagerService(
        appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
) {
    private val activity by LazyExtractor(appCompatActivity)

    private val dpi: Int
        get() {
            val metrics = activity.resources.displayMetrics
            return metrics.densityDpi
        }

    fun keepScreenOn(set: Boolean) {
        if (set) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        setShowWhenLocked(set)
    }

    fun hideTaskbar() {
        if (activity.supportActionBar != null) {
            activity.supportActionBar!!.hide()
        }
    }

    fun setFullscreen(set: Boolean) {
        val flag = WindowManager.LayoutParams.FLAG_FULLSCREEN
        if (set) {
            activity.window.addFlags(flag)
        } else {
            activity.window.clearFlags(flag)
        }
    }

    private fun setShowWhenLocked(set: Boolean) {
        val flag = WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        if (set) {
            activity.window.setFlags(flag, flag)
        } else {
            activity.window.clearFlags(flag)
        }
    }

    fun dp2px(dp: Float): Float {
        return dp * (dpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun showAppWhenLocked() {
        activity.window
                .addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
    }
}
