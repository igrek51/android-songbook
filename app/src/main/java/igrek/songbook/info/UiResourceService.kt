package igrek.songbook.info

import android.app.Activity
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

open class UiResourceService(
        activity: LazyInject<Activity> = appFactory.activity,
) {
    private val activity: Activity by LazyExtractor(activity)

    open fun resString(resourceId: Int): String {
        return activity.resources.getString(resourceId)
    }

    open fun resString(resourceId: Int, vararg args: Any?): String {
        val message = resString(resourceId)
        return if (args.isNotEmpty()) {
            String.format(message, *args)
        } else {
            message
        }
    }

    @ColorInt
    fun getColor(@ColorRes resourceId: Int): Int {
        return ContextCompat.getColor(activity, resourceId)
    }

}
