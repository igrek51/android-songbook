package igrek.songbook.layout.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class GenericListAdapter<T> internal constructor(
    context: Context,
    var dataSource: MutableList<T>,
    private val layoutRes: Int,
    private val viewBuilder: (view: View, item: T) -> Unit = { _, _ -> },
) : ArrayAdapter<T>(context, 0, dataSource) {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = dataSource[position]
        return createView(item, parent)
    }

    private fun createView(item: T, parent: ViewGroup): View {
        val itemView = inflater.inflate(layoutRes, parent, false)
        viewBuilder(itemView, item)
        return itemView
    }
}