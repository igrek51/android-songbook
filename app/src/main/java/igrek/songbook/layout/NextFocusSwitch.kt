package igrek.songbook.layout

import android.view.KeyEvent
import android.view.View
import igrek.songbook.info.logger.LoggerFactory.logger


class NextFocusSwitch (
    private val currentViewGetter: () -> View?,
    private val currentFocusGetter: () -> Int?,
    private val nextLeft: (Int, View) -> Int = { _, _ -> 0 },
    private val nextRight: (Int, View) -> Int = { _, _ -> 0 },
    private val nextUp: (Int, View) -> Int = { _, _ -> 0 },
    private val nextDown: (Int, View) -> Int = { _, _ -> 0 },
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

    private fun moveToNextView(nextViewProvider: (Int, View) -> Int): Boolean {
        val currentView: View = currentViewGetter() ?: return false
        val currentFocusId: Int = currentFocusGetter() ?: 0

        val nextViewId = nextViewProvider(currentFocusId, currentView)
        if (nextViewId != 0 && nextViewId != currentFocusId) {

            val nextView: View? = currentView.findViewById(nextViewId)
            if (nextView == null) {
                logger.warn("cant find next view with ID $nextViewId ")
            }
            nextView?.run {

//                val viewGroup = currentView as? ViewGroup
//                viewGroup?.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS

                val result = requestFocus()
                if (!result) {
                    logger.warn("requesting focus failed, focused: $isFocused, focusable: $isFocusable")
                }


//                var gainedFocus = false
//                this.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
//                    if (hasFocus)
//                        gainedFocus = true
//                    if (v == this && !hasFocus && gainedFocus) {
//                        viewGroup?.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
//                    }
//                }

                this.setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        if (this@NextFocusSwitch.handleKey(keyCode))
                            return@setOnKeyListener true
                    }
                    return@setOnKeyListener false
                }

                val nextViewName = nextView.javaClass.simpleName
                logger.debug("NextFocusSwitch: focus set to $nextViewId - $nextViewName")
                return true
            }
        }
        return false
    }

}
