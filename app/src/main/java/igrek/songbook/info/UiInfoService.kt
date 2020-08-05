package igrek.songbook.info

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import igrek.songbook.R
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.info.errorcheck.SafeExecutor
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


open class UiInfoService(
        activity: LazyInject<Activity> = appFactory.activity,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
) {
    private val activity by LazyExtractor(activity)
    private val uiResourceService by LazyExtractor(uiResourceService)

    private val infobars = HashMap<View?, Snackbar>()
    private val logger: Logger = LoggerFactory.logger

    /**
     * @param info       text to show or replace
     * @param view       view, on which the text should be displayed
     * @param actionName action button text value (if null - no action button)
     * @param action     action perforfmed on button click (if null - dismiss displayed snackbar)
     */
    protected open fun showActionInfo(info: String, view: View?, actionName: String?, action: (() -> Unit)?, color: Int?, snackbarLength: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            var viewV = view
            var actionV = action
            if (viewV == null)
                viewV = activity.findViewById(R.id.main_content)

            // dont create new snackbars if one is already shown
            var snackbar: Snackbar? = infobars[viewV]
            if (snackbar == null || !snackbar.isShown) { // a new one
                snackbar = Snackbar.make(viewV!!, info, snackbarLength)
                snackbar.setActionTextColor(Color.WHITE)
            } else { // visible - use it one more time
                snackbar.duration = snackbarLength
                snackbar.setText(info)
            }

            if (actionName != null) {
                if (actionV == null) {
                    val finalSnackbar = snackbar
                    actionV = { finalSnackbar.dismiss() }
                }

                snackbar.setAction(actionName, SafeClickListener {
                    actionV.invoke()
                })
                if (color != null) {
                    snackbar.setActionTextColor(color)
                }
            }

            snackbar.show()
            infobars[viewV] = snackbar
        }
        logger.debug("UI: snackbar: $info")
    }

    fun showInfo(info: String, dismissName: String) {
        showActionInfo(info, null, dismissName, null, null, Snackbar.LENGTH_LONG)
    }

    open fun showInfo(info: String) {
        val dismissName = uiResourceService.resString(R.string.action_info_ok)
        showInfo(info, dismissName)
    }

    open fun showInfo(infoRes: Int, vararg args: String) {
        val info = uiResourceService.resString(infoRes, *args)
        showInfo(info)
    }

    open fun showInfoIndefinite(info: String) {
        val dismissName = uiResourceService.resString(R.string.action_info_ok)
        showActionInfo(info, null, dismissName, null, null, Snackbar.LENGTH_INDEFINITE)
    }

    open fun showInfoIndefinite(infoRes: Int, vararg args: String) {
        val info = uiResourceService.resString(infoRes, *args)
        showInfoIndefinite(info)
    }

    protected open fun showInfoWithAction(info: String, actionName: String, actionCallback: (() -> Unit), snackbarLength: Int) {
        val color = ContextCompat.getColor(activity, R.color.colorAccent)
        showActionInfo(info, null, actionName, actionCallback, color, snackbarLength)
    }

    open fun showInfoWithAction(info: String, actionNameRes: Int, actionCallback: (() -> Unit)) {
        val actionName = uiResourceService.resString(actionNameRes)
        showInfoWithAction(info, actionName, actionCallback, Snackbar.LENGTH_LONG)
    }

    open fun showInfoWithAction(infoRes: Int, actionNameRes: Int, actionCallback: (() -> Unit)) {
        val info = uiResourceService.resString(infoRes)
        val actionName = uiResourceService.resString(actionNameRes)
        showInfoWithAction(info, actionName, actionCallback, Snackbar.LENGTH_LONG)
    }

    open fun showInfoWithActionIndefinite(infoRes: Int, actionNameRes: Int, actionCallback: (() -> Unit)) {
        val info = uiResourceService.resString(infoRes)
        val actionName = uiResourceService.resString(actionNameRes)
        showInfoWithAction(info, actionName, actionCallback, Snackbar.LENGTH_INDEFINITE)
    }

    open fun showToast(message: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(activity.applicationContext, message, Toast.LENGTH_LONG).show()
        }
        logger.debug("UI: toast: $message")
    }

    open fun showToast(messageRes: Int) {
        val message = uiResourceService.resString(messageRes)
        showToast(message)
    }

    open fun showTooltip(infoRes: Int) {
        dialog(R.string.tooltip, infoRes)
    }

    fun clearSnackBars() {
        infobars.clear()
    }

    open fun resString(resourceId: Int, vararg args: Any?): String =
            uiResourceService.resString(resourceId, *args)

    fun dialog(titleResId: Int, message: String) {
        dialogThreeChoices(
                titleResId = titleResId,
                message = message,
                positiveButton = R.string.action_info_ok, positiveAction = {},
        )
    }

    fun dialog(titleResId: Int, messageResId: Int) {
        dialogThreeChoices(
                titleResId = titleResId,
                messageResId = messageResId,
                positiveButton = R.string.action_info_ok, positiveAction = {},
        )
    }

    fun dialogThreeChoices(
            titleResId: Int = 0, title: String = "",
            messageResId: Int = 0, message: String = "",
            positiveButton: Int = 0, positiveAction: () -> Unit = {},
            negativeButton: Int = 0, negativeAction: () -> Unit = {},
            neutralButton: Int = 0, neutralAction: () -> Unit = {},
            postProcessor: (AlertDialog) -> Unit = {},
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val alertBuilder = AlertDialog.Builder(activity)

            alertBuilder.setMessage(when {
                messageResId > 0 -> uiResourceService.resString(messageResId)
                else -> message
            })
            alertBuilder.setTitle(when {
                titleResId > 0 -> uiResourceService.resString(titleResId)
                else -> title
            })

            if (positiveButton > 0) {
                alertBuilder.setPositiveButton(uiResourceService.resString(positiveButton)) { _, _ ->
                    SafeExecutor {
                        positiveAction.invoke()
                    }
                }
            }
            if (negativeButton > 0) {
                alertBuilder.setNegativeButton(uiResourceService.resString(negativeButton)) { _, _ ->
                    SafeExecutor {
                        negativeAction.invoke()
                    }
                }
            }
            if (neutralButton > 0) {
                alertBuilder.setNeutralButton(uiResourceService.resString(neutralButton)) { _, _ ->
                    SafeExecutor {
                        neutralAction.invoke()
                    }
                }
            }
            alertBuilder.setCancelable(true)
            val alertDialog = alertBuilder.create()
            postProcessor.invoke(alertDialog)
            if (!activity.isFinishing) {
                alertDialog.show()
            }
        }
    }

}
