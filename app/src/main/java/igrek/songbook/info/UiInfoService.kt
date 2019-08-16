package igrek.songbook.info

import android.app.Activity
import android.graphics.Color
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.errorcheck.SafeClickListener
import java.util.*
import javax.inject.Inject


class UiInfoService {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var uiResourceService: dagger.Lazy<UiResourceService>

    private val infobars = HashMap<View?, Snackbar>()

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    /**
     * @param info       text to show or replace
     * @param view       view, on which the text should be displayed
     * @param actionName action button text value (if null - no action button)
     * @param action     action perforfmed on button click (if null - dismiss displayed snackbar)
     */
    private fun showActionInfo(info: String, view: View?, actionName: String?, action: (() -> Unit)?, color: Int?, snackbarLength: Int) {
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

    fun showInfo(info: String, dismissName: String) {
        showActionInfo(info, null, dismissName, null, null, Snackbar.LENGTH_LONG)
    }

    fun showInfo(info: String) {
        val dismissName = uiResourceService.get().resString(R.string.action_info_ok)
        showInfo(info, dismissName)
    }

    fun showInfo(infoRes: Int, vararg args: String) {
        val info = uiResourceService.get().resString(infoRes, *args)
        showInfo(info)
    }

    private fun showInfoIndefinite(info: String) {
        val dismissName = uiResourceService.get().resString(R.string.action_info_ok)
        showActionInfo(info, null, dismissName, null, null, Snackbar.LENGTH_INDEFINITE)
    }

    fun showInfoIndefinite(infoRes: Int) {
        val info = uiResourceService.get().resString(infoRes)
        showInfoIndefinite(info)
    }

    private fun showInfoWithAction(info: String, actionName: String, actionCallback: (() -> Unit), snackbarLength: Int) {
        val color = ContextCompat.getColor(activity, R.color.colorAccent)
        showActionInfo(info, null, actionName, actionCallback, color, snackbarLength)
    }

    fun showInfoWithAction(info: String, actionNameRes: Int, actionCallback: (() -> Unit)) {
        val actionName = uiResourceService.get().resString(actionNameRes)
        showInfoWithAction(info, actionName, actionCallback, Snackbar.LENGTH_LONG)
    }

    fun showInfoWithAction(infoRes: Int, actionNameRes: Int, actionCallback: (() -> Unit)) {
        val info = uiResourceService.get().resString(infoRes)
        val actionName = uiResourceService.get().resString(actionNameRes)
        showInfoWithAction(info, actionName, actionCallback, Snackbar.LENGTH_LONG)
    }

    fun showInfoWithActionIndefinite(infoRes: Int, actionNameRes: Int, actionCallback: (() -> Unit)) {
        val info = uiResourceService.get().resString(infoRes)
        val actionName = uiResourceService.get().resString(actionNameRes)
        showInfoWithAction(info, actionName, actionCallback, Snackbar.LENGTH_INDEFINITE)
    }

    fun showToast(message: String) {
        Toast.makeText(activity.applicationContext, message, Toast.LENGTH_LONG).show()
    }

    fun showToast(messageRes: Int) {
        val message = uiResourceService.get().resString(messageRes)
        showToast(message)
    }

    fun showDialog(title: String, message: String) {
        val alertBuilder = AlertDialog.Builder(activity)
        alertBuilder.setMessage(message)
        alertBuilder.setTitle(title)
        alertBuilder.setPositiveButton(uiResourceService.get().resString(R.string.action_info_ok)) { _, _ -> }
        alertBuilder.setCancelable(true)
        alertBuilder.create().show()
    }

    fun showTooltip(infoRes: Int) {
        val message = uiResourceService.get().resString(infoRes)
        val title = uiResourceService.get().resString(R.string.tooltip)
        showDialog(title, message)
    }

    fun clearSnackBars() {
        infobars.clear()
    }

}
