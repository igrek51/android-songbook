package igrek.songbook.layout.spinner

import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SinglePicker<T>(
    private val context: Context,
    private val entityNames: LinkedHashMap<T, String>,
    selected: T,
    private val title: String,
    private val onChange: (T) -> Unit,
) : OnClickListener {

    private var selectedKey: T = selected

    fun showChoiceDialog() {
        val that = this
        GlobalScope.launch(Dispatchers.Main) {
            val namesArray = entityNames.values.toTypedArray()
            val selectedIndex = entityNames.keys.indexOf(selectedKey)

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setSingleChoiceItems(namesArray, selectedIndex, that)

            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                onChange.invoke(selectedKey)
            }
            builder.show()
        }
    }

    fun getSelected(): T {
        return selectedKey
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        selectedKey = entityNames.keys.elementAt(which)
        dialog?.dismiss()
        onChange.invoke(selectedKey)
    }
}