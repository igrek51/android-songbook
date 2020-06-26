package igrek.songbook.layout.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import java.util.*

class GenericListAdapter<T> internal constructor(
        context: Context,
        var dataSource: List<T>,
        private val layoutRes: Int,
        private val viewBuilder: (view: View, item: T) -> Unit = { _, _ -> },
) : ArrayAdapter<T>(context, 0, ArrayList()) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItem(position: Int): T? {
        return dataSource[position]
    }

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItemId(position: Int): Long {
        if (position < 0)
            return -1
        return if (position >= dataSource.size) -1 else position.toLong()
    }

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