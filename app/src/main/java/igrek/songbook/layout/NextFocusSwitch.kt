package igrek.songbook.layout

import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import igrek.songbook.info.logger.LoggerFactory.logger

class NextFocusSwitch(
    private val currentViewGetter: () -> View?,
    private val nextRight: (Int) -> Int = { 0 },
    private val nextLeft: (Int) -> Int = { 0 },
    private val nextDown: (Int) -> Int = { 0 },
    private val nextUp: (Int) -> Int = { 0 },
) {

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

    private fun moveToNextView(nextViewProvider: (Int) -> Int): Boolean {
        val currentView: View = currentViewGetter() ?: return false
        val focusView: View? = currentView.findFocus()
        if (focusView == null) {
            logger.warn("NextFocusSwitch: no sub view with focus")
        }
        val currentFocusId = focusView?.id ?: 0

        val focusedViewName = focusView?.javaClass?.simpleName
        logger.debug("NextFocusSwitch: current focus view id: $currentFocusId - $focusedViewName")

        val nextViewId = nextViewProvider(currentFocusId)
        if (nextViewId != 0 && nextViewId != currentFocusId) {

            val nextView: View? = currentView.findViewById(nextViewId)
            if (nextView == null) {
                logger.warn("didnt find view $nextViewId ")
            }
            nextView?.run {

                val viewGroup = currentView as ViewGroup
                viewGroup.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS

                requestFocus()
                logger.debug("focus set to $nextViewId ")
                return true
            }
        }
        return false
    }

}