package igrek.songbook.layout.contextmenu

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.safeExecute
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ContextMenuBuilder(
    activity: LazyInject<Activity> = appFactory.activity,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
) {
    private val activity by LazyExtractor(activity)
    private val uiResourceService by LazyExtractor(uiResourceService)

    fun showContextMenu(titleResId: Int, actions: List<Action>) {
        GlobalScope.launch(Dispatchers.Main) {
            val actionNames = actions.map { action -> actionName(action) }.toTypedArray()

            val builder = AlertDialog.Builder(activity)
                .setTitle(uiResourceService.resString(titleResId))
                .setItems(actionNames) { _, item ->
                    safeExecute {
                        actions[item].executor()
                    }
                }
                .setCancelable(true)
            if (!activity.isFinishing) {
                builder.create().show()
            }
        }
    }

    fun showContextMenu(actions: List<Action>) {
        GlobalScope.launch(Dispatchers.Main) {
            val actionNames = actions.map { action -> actionName(action) }.toTypedArray()

            val builder = AlertDialog.Builder(activity)
                .setItems(actionNames) { _, item ->
                    safeExecute {
                        actions[item].executor()
                    }
                }
                .setCancelable(true)
            if (!activity.isFinishing) {
                builder.create().show()
            }
        }
    }

    private fun actionName(action: Action): String {
        if (action.name == null) {
            action.name = uiResourceService.resString(action.nameResId!!)
        }
        return action.name!!
    }

    data class Action(
        var name: String?,
        val nameResId: Int?,
        val executor: () -> Unit,
    ) {

        constructor(name: String, executor: () -> Unit) : this(name, null, executor)

        constructor(nameResId: Int, executor: () -> Unit) : this(null, nameResId, executor)

    }


}