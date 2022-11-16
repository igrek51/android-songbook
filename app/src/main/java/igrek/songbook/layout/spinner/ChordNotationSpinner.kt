package igrek.songbook.layout.spinner

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.settings.chordsnotation.ChordsNotation

class ChordNotationSpinner(
    @IdRes spinnerId: Int,
    layout: View,
    activity: AppCompatActivity,
    chordsNotationDisplayNames: LinkedHashMap<ChordsNotation, String>
) {

    var selectedNotation: ChordsNotation
        get() = internalValue
        set(value) {
            selectEntity(value)
        }

    val spinner: Spinner = layout.findViewById(spinnerId)

    private var internalValue: ChordsNotation = ChordsNotation.default

    private val availableEntities: List<ChordsNotation> by lazy {
        chordsNotationDisplayNames.keys.toList()
    }

    private val displayItems: Array<String> by lazy {
        chordsNotationDisplayNames.values.toTypedArray()
    }

    init {
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, displayItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View,
                position: Int, id: Long
            ) {
                internalValue = availableEntities[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun selectEntity(value: ChordsNotation) {
        internalValue = value
        val index = availableEntities.indexOf(value)
        spinner.setSelection(index)
    }
}