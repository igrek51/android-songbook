package igrek.songbook.layout

import android.app.Activity
import android.widget.ImageButton
import igrek.songbook.R
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

class NextFocusTraverser(
    activity: LazyInject<Activity> = appFactory.activity,
) {
    private val activity by LazyExtractor(activity)

    fun moveToNextView(nextViewProvider: (Int) -> Int): Boolean {
        val currentFocusId = activity.currentFocus?.id ?: 0
        val nextViewId = nextViewProvider(currentFocusId)
        if (nextViewId != 0 && nextViewId != currentFocusId) {
            activity.findViewById<ImageButton>(nextViewId)?.run {
                requestFocus()
            }
            return true
        }
        return false
    }

    fun nextRightView(currentViewId: Int): Int {
        return when (currentViewId) {
            R.id.navMenuButton -> R.id.languageFilterButton
            R.id.languageFilterButton -> R.id.searchSongButton
            R.id.searchSongButton -> 0
            else -> R.id.navMenuButton
        }
    }

    fun nextLeftView(currentViewId: Int): Int {
        return when (currentViewId) {
            R.id.navMenuButton -> 0
            R.id.languageFilterButton -> R.id.navMenuButton
            R.id.searchSongButton -> R.id.languageFilterButton
            else -> R.id.navMenuButton
        }
    }

    fun nextDownView(currentViewId: Int): Int {
        return when (currentViewId) {
            R.id.navMenuButton -> 0
            else -> R.id.navMenuButton
        }
    }

    fun nextUpView(currentViewId: Int): Int {
        return when (currentViewId) {
            R.id.navMenuButton -> 0
            else -> R.id.navMenuButton
        }
    }
}