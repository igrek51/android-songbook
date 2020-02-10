package igrek.songbook.admin.antechamber

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import igrek.songbook.R
import java.util.*

class AntechamberSongListAdapter internal constructor(
        context: Context,
        _dataSource: List<AntechamberSong>?
) : ArrayAdapter<AntechamberSong>(context, 0, ArrayList()) {

    private var dataSource: List<AntechamberSong>? = null
    private val inflater: LayoutInflater
    private var onMore: ((AntechamberSong) -> Unit)? = null

    init {
        var dataSource = _dataSource
        if (dataSource == null)
            dataSource = ArrayList()
        this.dataSource = dataSource
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setDataSource(dataSource: List<AntechamberSong>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): AntechamberSong? {
        return dataSource!![position]
    }

    override fun getCount(): Int {
        return dataSource!!.size
    }

    override fun getItemId(position: Int): Long {
        if (position < 0)
            return -1
        return if (position >= dataSource!!.size) -1 else position.toLong()
    }

    fun setOnMoreListener(onMore: (item: AntechamberSong) -> Unit) {
        this.onMore = onMore
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = dataSource!![position]
        return createItemView(item, parent)
    }

    private fun createItemView(item: AntechamberSong, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.list_item_antichamber_song, parent, false)
        val itemSongTitleLabel = itemView.findViewById<TextView>(R.id.itemSongTitleLabel)

        var title = "${item.id} - ${item.title}"
        if (!item.categoryName.isNullOrEmpty()) {
            title += " - ${item.categoryName}"
        }
        itemSongTitleLabel.text = title

        itemView.findViewById<ImageButton>(R.id.itemSongMoreButton).setOnClickListener {
            onMore?.invoke(item)
        }
        return itemView
    }
}