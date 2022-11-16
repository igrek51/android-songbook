package igrek.songbook.layout.spinner

import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import androidx.appcompat.app.AlertDialog
import igrek.songbook.R


class MultiPicker<T>(
    private val context: Context,
    private val entityNames: LinkedHashMap<T, String>,
    selected: Set<T>,
    private val title: String,
    private val onChange: (Set<T>) -> Unit,
) : OnMultiChoiceClickListener {

    private var selected = HashSet<T>(selected)

    fun showChoiceDialog() {
        val namesArray = entityNames.values.toTypedArray()
        val valuesArray: BooleanArray =
            orderedKeys().map { key -> key in selected }.toBooleanArray()

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMultiChoiceItems(namesArray, valuesArray, this)
        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
            onChange.invoke(selected)
        }
        builder.setNeutralButton(R.string.multichoice_invert) { dialog, _ ->
            invertSelection()
            showChoiceDialog()
            dialog.dismiss()
        }
        builder.show()
    }

    fun getSelected(): Set<T> {
        return selected
    }

    private fun orderedKeys(): List<T> {
        return entityNames.keys.toList()
    }

    private fun invertSelection() {
        val notSelected = orderedKeys().filter { it !in selected }.toHashSet()
        selected = notSelected
    }

    override fun onClick(dialog: DialogInterface, which: Int, isChecked: Boolean) {
        val entity = orderedKeys()[which]
        if (isChecked) {
            selected.add(entity)
        } else {
            selected.remove(entity)
        }
    }
}