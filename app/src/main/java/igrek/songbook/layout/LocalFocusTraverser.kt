package igrek.songbook.layout

import android.app.Activity
import android.view.KeyEvent
import android.view.View
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory


class LocalFocusTraverser(
    private val currentViewGetter: () -> View?,
    private val currentFocusGetter: () -> Int?,
    private val preNextFocus: (Int, View) -> Int = { _, _ -> 0 },
    private val nextLeft: (Int, View) -> Int = { _, _ -> 0 },
    private val nextRight: (Int, View) -> Int = { _, _ -> 0 },
    private val nextUp: (Int, View) -> Int = { _, _ -> 0 },
    private val nextDown: (Int, View) -> Int = { _, _ -> 0 },
    activity: LazyInject<Activity> = appFactory.activity,
) {
    private val activity by LazyExtractor(activity)

    private val debugMode: Boolean = true

    fun handleKey(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                return moveToNextView(nextUp)
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                return moveToNextView(nextDown)
            }
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_MEDIA_REWIND,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD,
            KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD -> {
                return moveToNextView(nextLeft)
            }
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD,
            KeyEvent.KEYCODE_MEDIA_STEP_FORWARD -> {
                return moveToNextView(nextRight)
            }
        }
        return false
    }

    private fun moveToNextView(nextViewProvider: (Int, View) -> Int): Boolean {
        val currentView: View = currentViewGetter() ?: return false
        val currentFocusId: Int = currentFocusGetter() ?: 0

        if (debugMode && currentFocusId > 0) {
            val viewName = activity.currentFocus?.javaClass?.simpleName
            val resourceName = activity.resources.getResourceName(currentFocusId)
            logger.debug("Current focus view: $resourceName - $viewName")
        }

        var nextViewId = preNextFocus(currentFocusId, currentView)
        if (nextViewId == 0) {
            nextViewId = nextViewProvider(currentFocusId, currentView)
        }

        if (nextViewId == -1)
            return true

        if (nextViewId != 0 && nextViewId != currentFocusId) {

            var nextView: View? = currentView.findViewById(nextViewId)
            if (nextView == null) {
                nextView = activity.findViewById(nextViewId)
                if (nextView == null)
                    logger.warn("cant find next view with ID $nextViewId ")
            }
            nextView?.let {
                val nextViewClass = it.javaClass.simpleName

                if (it.isClickable)
                    it.isFocusableInTouchMode = true
                val result = it.requestFocusFromTouch()
                if (it.isClickable)
                    it.isFocusableInTouchMode = false

                if (!result && debugMode && nextViewId > 0) {
                    val nextResourceName = activity.resources.getResourceName(nextViewId)
                    logger.warn("requesting focus failed for $nextResourceName - $nextViewClass")
                }

                it.setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        if (this@LocalFocusTraverser.handleKey(keyCode))
                            return@setOnKeyListener true
                    }
                    return@setOnKeyListener false
                }

                if (result && debugMode && nextViewId > 0) {
                    val nextResourceName = activity.resources.getResourceName(nextViewId)
                    logger.debug("focus set to $nextResourceName - $nextViewClass")
                }
                return result
            }
        }
        return false
    }

}
