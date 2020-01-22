package igrek.songbook.layout.contextmenu

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.SafeExecutor
import javax.inject.Inject

class ContextMenuBuilder {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var uiResourceService: UiResourceService

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun showContextMenu(titleResId: Int, actions: List<Action>) {
        val actionNames = actions.map { action -> actionName(action) }.toTypedArray()

        val builder = AlertDialog.Builder(activity)
                .setTitle(uiResourceService.resString(titleResId))
                .setItems(actionNames) { _, item ->
                    SafeExecutor().execute {
                        actions[item].executor()
                    }
                }
                .setCancelable(true)
        builder.create().show()
    }

    private fun actionName(action: Action): String {
        if (action.name == null) {
            action.name = uiResourceService.resString(action.nameResId!!)
        }
        return action.name!!
    }

    data class Action(var name: String?,
                      val nameResId: Int?,
                      val executor: () -> Unit) {

        constructor(name: String, executor: () -> Unit) : this(name, null, executor)

        constructor(nameResId: Int, executor: () -> Unit) : this(null, nameResId, executor)

    }


}