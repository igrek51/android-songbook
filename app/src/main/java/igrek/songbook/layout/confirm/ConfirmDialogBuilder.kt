package igrek.songbook.layout.confirm

import android.app.Activity
import android.support.v7.app.AlertDialog
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import javax.inject.Inject

class ConfirmDialogBuilder {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var uiResourceService: UiResourceService

    init {
        DaggerIoc.getFactoryComponent().inject(this)
    }

    fun confirmAction(messageResId: Int, action: () -> Unit) {
        val alertBuilder = AlertDialog.Builder(activity)
        val message = uiResourceService.resString(messageResId)
        alertBuilder.setMessage(message)
        alertBuilder.setTitle(uiResourceService.resString(R.string.action_confirmation_title))
        alertBuilder.setNegativeButton(uiResourceService.resString(R.string.action_cancel)) { _, _ -> }
        alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_info_ok)) { _, _ ->
            SafeExecutor().execute {
                action.invoke()
            }
        }
        alertBuilder.setCancelable(true)
        alertBuilder.create().show()
    }

}