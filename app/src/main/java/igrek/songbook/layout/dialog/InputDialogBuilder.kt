package igrek.songbook.layout.dialog

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import igrek.songbook.R
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.system.SoftKeyboardService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class InputDialogBuilder(
        activity: LazyInject<Activity> = appFactory.activity,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
        softKeyboardService: LazyInject<SoftKeyboardService> = appFactory.softKeyboardService,
) {
    private val activity by LazyExtractor(activity)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val softKeyboardService by LazyExtractor(softKeyboardService)

    fun input(title: String, initialValue: String?, action: (String) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            val alertBuilder = AlertDialog.Builder(activity)
            alertBuilder.setTitle(title)

            val input = EditText(activity)
            input.inputType = InputType.TYPE_CLASS_TEXT
            if (initialValue != null)
                input.setText(initialValue)
            alertBuilder.setView(input)

            alertBuilder.setNegativeButton(uiResourceService.resString(R.string.action_cancel)) { _, _ -> }
            alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_info_ok)) { _, _ ->
                softKeyboardService.hideSoftKeyboard(input)
                Handler(Looper.getMainLooper()).post {
                    softKeyboardService.hideSoftKeyboard()
                }
                SafeExecutor {
                    action.invoke(input.text.toString())
                }
            }
            alertBuilder.setCancelable(true)
            if (!activity.isFinishing) {
                val dialog = alertBuilder.create()
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                dialog.show()
            }

            input.requestFocus()
            Handler(Looper.getMainLooper()).post {
                softKeyboardService.showSoftKeyboard(input)
            }
        }
    }

    fun input(titleResId: Int, initialValue: String?, action: (String) -> Unit) {
        val title = uiResourceService.resString(titleResId)
        input(title, initialValue, action)
    }

}