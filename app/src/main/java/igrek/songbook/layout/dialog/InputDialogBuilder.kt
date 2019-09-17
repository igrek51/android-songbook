package igrek.songbook.layout.dialog

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.widget.EditText
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.system.SoftKeyboardService
import javax.inject.Inject

class InputDialogBuilder {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var softKeyboardService: SoftKeyboardService

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun input(title: String, initialValue: String?, action: (String) -> Unit) {
        val alertBuilder = AlertDialog.Builder(activity)
        alertBuilder.setTitle(title)

        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_TEXT
        if (initialValue != null)
            input.setText(initialValue)
        alertBuilder.setView(input)

        alertBuilder.setNegativeButton(uiResourceService.resString(R.string.action_cancel)) { _, _ -> }
        alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_info_ok)) { _, _ ->
            SafeExecutor().execute {
                action.invoke(input.text.toString())
            }
        }
        alertBuilder.setCancelable(true)
        alertBuilder.create().show()

        Handler(Looper.getMainLooper()).post {
            softKeyboardService.showSoftKeyboard(input)
        }
    }

    fun input(titleResId: Int, initialValue: String?, action: (String) -> Unit) {
        val title = uiResourceService.resString(titleResId)
        input(title, initialValue, action)
    }

}