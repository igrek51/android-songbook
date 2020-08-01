package igrek.songbook.layout.dialog

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import igrek.songbook.R
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory

class ConfirmDialogBuilder(
        activity: LazyInject<Activity> = appFactory.activity,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
) {
    private val activity by LazyExtractor(activity)
    private val uiResourceService by LazyExtractor(uiResourceService)

    fun confirmAction(message: String, action: () -> Unit) {
        val alertBuilder = AlertDialog.Builder(activity)
        alertBuilder.setMessage(message)
        alertBuilder.setTitle(uiResourceService.resString(R.string.action_confirmation_title))
        alertBuilder.setNegativeButton(uiResourceService.resString(R.string.action_cancel)) { _, _ -> }
        alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_info_ok)) { _, _ ->
            SafeExecutor {
                action.invoke()
            }
        }
        alertBuilder.setCancelable(true)
        if (!activity.isFinishing) {
            alertBuilder.create().show()
        }
    }

    fun confirmAction(messageResId: Int, action: () -> Unit) {
        val message = uiResourceService.resString(messageResId)
        confirmAction(message, action)
    }

    fun chooseFromThree(messageId: Int, titleResId: Int,
                        positiveButton: Int, positiveAction: () -> Unit,
                        negativeButton: Int, negativeAction: () -> Unit,
                        neutralButton: Int, neutralAction: () -> Unit) {
        val alertBuilder = AlertDialog.Builder(activity)

        val message = uiResourceService.resString(messageId)
        alertBuilder.setMessage(message)
        val title = uiResourceService.resString(titleResId)
        alertBuilder.setTitle(title)

        alertBuilder.setPositiveButton(uiResourceService.resString(positiveButton)) { _, _ ->
            SafeExecutor {
                positiveAction.invoke()
            }
        }
        alertBuilder.setNegativeButton(uiResourceService.resString(negativeButton)) { _, _ ->
            SafeExecutor {
                negativeAction.invoke()
            }
        }
        alertBuilder.setNeutralButton(uiResourceService.resString(neutralButton)) { _, _ ->
            SafeExecutor {
                neutralAction.invoke()
            }
        }
        alertBuilder.setCancelable(true)
        if (!activity.isFinishing) {
            alertBuilder.create().show()
        }
    }

}