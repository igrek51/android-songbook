package igrek.songbook.info

import android.app.Activity
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.google.android.material.snackbar.Snackbar
import igrek.songbook.R
import igrek.songbook.info.errorcheck.SafeClickListener
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


open class UiInfoService(
    activity: LazyInject<Activity> = appFactory.activity,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
) {
    private val activity by LazyExtractor(activity)
    private val uiResourceService by LazyExtractor(uiResourceService)

    private val infobars = HashMap<View?, Snackbar>()
    private var lastSnakbar: Snackbar? = null
    private val logger: Logger = LoggerFactory.logger

    open fun showSnackbar(
        info: String = "",
        infoResId: Int = 0,
        actionResId: Int = 0,
        indefinite: Boolean = false,
        durationMillis: Int = 0,
        action: (() -> Unit)? = null, // dissmiss by default
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val duration = when {
                indefinite -> Snackbar.LENGTH_INDEFINITE
                durationMillis > 0 -> durationMillis
                else -> Snackbar.LENGTH_LONG
            }
            val infoV = info.takeIf { it.isNotEmpty() } ?: uiResourceService.resString(infoResId)

            // dont create new snackbars if one is already shown
            val view: View? = activity.findViewById(R.id.main_content_container)
            if (view == null) {
                logger.error("No main content view")
                Toast.makeText(activity.applicationContext, infoV, Toast.LENGTH_LONG).show()
                return@launch
            }
            var snackbar: Snackbar? = infobars[view]
            if (snackbar == null || !snackbar.isShown) { // a new one
                snackbar = Snackbar.make(view, infoV, duration)
            } else { // visible - reuse it one more time
                snackbar.duration = duration
                snackbar.setText(infoV)
            }

            if (actionResId > 0) {
                val actionV: () -> Unit = action ?: snackbar::dismiss
                val actionName = uiResourceService.resString(actionResId)
                snackbar.setAction(actionName, SafeClickListener {
                    actionV.invoke()
                })
                val color = ContextCompat.getColor(activity, R.color.colorAccent)
                snackbar.setActionTextColor(color)
            }

            snackbar.show()
            lastSnakbar = snackbar
            infobars[view] = snackbar
        }
        logger.info("UI: snackbar: $info")
    }

    fun showInfo(
        infoResId: Int, vararg args: String?,
        indefinite: Boolean = false,
    ) {
        val info = uiResourceService.resString(infoResId, *args)
        showSnackbar(info = info, actionResId = R.string.action_info_ok, indefinite = indefinite)
    }

    fun showInfo(
        info: String,
        indefinite: Boolean = false,
    ) {
        showSnackbar(info = info, actionResId = R.string.action_info_ok, indefinite = indefinite)
    }

    fun showInfoAction(
        infoResId: Int,
        vararg args: String,
        actionResId: Int,
        indefinite: Boolean = false,
        durationMillis: Int = 0,
        action: () -> Unit,
    ) {
        val info = uiResourceService.resString(infoResId, *args)
        showSnackbar(
            info = info,
            actionResId = actionResId,
            action = action,
            indefinite = indefinite,
            durationMillis = durationMillis,
        )
    }

    fun isSnackbarShown(): Boolean {
        return lastSnakbar?.isShown ?: false
    }

    fun focusSnackBar(): Boolean {
        if (!isSnackbarShown())
            return false
        val actionView =
            lastSnakbar?.view?.findViewById<View>(com.google.android.material.R.id.snackbar_action)
        return actionView?.requestFocusFromTouch() ?: false
    }


    open fun showToast(message: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(activity.applicationContext, message, Toast.LENGTH_LONG).show()
        }
        logger.debug("UI: toast: $message")
    }

    fun showToast(messageRes: Int) {
        val message = uiResourceService.resString(messageRes)
        showToast(message)
    }

    fun clearSnackBars() {
        GlobalScope.launch(Dispatchers.Main) {
            infobars.forEach { (_, snackbar) ->
                if (snackbar.isShown)
                    snackbar.dismiss()
            }
            infobars.clear()
        }
    }

    open fun resString(resourceId: Int, vararg args: Any?): String =
        uiResourceService.resString(resourceId, *args)

    fun resRichString(resourceId: Int, vararg args: Any?): Spanned {
        val text = uiResourceService.resString(resourceId, *args)
        return HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

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
        messageResId: Int = 0, message: CharSequence = "",
        positiveButton: Int = 0, positiveAction: () -> Unit = {},
        negativeButton: Int = 0, negativeAction: () -> Unit = {},
        neutralButton: Int = 0, neutralAction: () -> Unit = {},
        postProcessor: (AlertDialog) -> Unit = {},
        richMessage: Boolean = false,
        cancelable: Boolean = true,
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val alertBuilder = AlertDialog.Builder(activity)

            alertBuilder.setMessage(
                when {
                    messageResId > 0 -> uiResourceService.resString(messageResId)
                    else -> message
                }
            )
            alertBuilder.setTitle(
                when {
                    titleResId > 0 -> uiResourceService.resString(titleResId)
                    else -> title
                }
            )

            if (positiveButton > 0) {
                alertBuilder.setPositiveButton(uiResourceService.resString(positiveButton)) { _, _ ->
                    safeExecute {
                        positiveAction.invoke()
                    }
                }
            }
            if (negativeButton > 0) {
                alertBuilder.setNegativeButton(uiResourceService.resString(negativeButton)) { _, _ ->
                    safeExecute {
                        negativeAction.invoke()
                    }
                }
            }
            if (neutralButton > 0) {
                alertBuilder.setNeutralButton(uiResourceService.resString(neutralButton)) { _, _ ->
                    safeExecute {
                        neutralAction.invoke()
                    }
                }
            }
            alertBuilder.setCancelable(cancelable)
            val alertDialog = alertBuilder.create()
            postProcessor.invoke(alertDialog)
            if (!activity.isFinishing) {
                alertDialog.show()
            }
            if (richMessage) {
                val textView = alertDialog.findViewById<TextView>(android.R.id.message)
                textView?.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

}
