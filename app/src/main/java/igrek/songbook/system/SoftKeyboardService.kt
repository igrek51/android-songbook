package igrek.songbook.system

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import javax.inject.Inject

class SoftKeyboardService {

    @Inject
    lateinit var activity: AppCompatActivity

    private val imm: InputMethodManager?
    private val logger = LoggerFactory.logger

    init {
        DaggerIoc.factoryComponent.inject(this)
        imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    fun hideSoftKeyboard(view: View?) {
        if (imm == null) {
            logger.error("no input method manager")
            return
        }
        if (view == null) {
            logger.error("view = null")
            return
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun hideSoftKeyboard() {
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        hideSoftKeyboard(view)
    }

    fun showSoftKeyboard(view: View?) {
        imm?.showSoftInput(view, 0)
    }
}
