package igrek.songbook.songselection.listview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.tree.SongTreeItem
import java.util.*

class SongListItemAdapter internal constructor(
        context: Context,
        private var _dataSource: List<SongTreeItem> = emptyList(),
        songContextMenuBuilder: SongContextMenuBuilder,
) : ArrayAdapter<SongTreeItem>(context, 0, ArrayList()) {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val viewFactory: SongItemViewFactory = SongItemViewFactory(inflater, songContextMenuBuilder)

    fun setDataSource(dataSource: List<SongTreeItem>) {
        this._dataSource = dataSource
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): SongTreeItem? {
        return _dataSource[position]
    }

    override fun getCount(): Int {
        return _dataSource.size
    }

    override fun getItemId(position: Int): Long {
        return when {
            position < 0 -> -1
            position >= _dataSource.size -> -1
            else -> position.toLong()
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = _dataSource[position]
        return viewFactory.getView(item, parent)
    }
}