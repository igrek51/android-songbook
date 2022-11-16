package igrek.songbook.admin.antechamber

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.persistence.general.model.Song

class AntechamberSongListAdapter internal constructor(
    context: Context,
    _dataSource: List<Song>?
) : ArrayAdapter<Song>(context, 0, ArrayList()) {

    private var dataSource: List<Song>? = null
    private val inflater: LayoutInflater
    private var onMore: ((Song) -> Unit)? = null

    init {
        var dataSource = _dataSource
        if (dataSource == null)
            dataSource = ArrayList()
        this.dataSource = dataSource
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setDataSource(dataSource: List<Song>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): Song {
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

    fun setOnMoreListener(onMore: (item: Song) -> Unit) {
        this.onMore = onMore
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = dataSource!![position]
        return createItemView(item, parent)
    }

    private fun createItemView(item: Song, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.list_item_antichamber_song, parent, false)
        val itemSongTitleLabel = itemView.findViewById<TextView>(R.id.itemSongTitleLabel)

        var title = "${item.id} - ${item.title}"
        if (!item.customCategoryName.isNullOrEmpty()) {
            title += " - ${item.customCategoryName}"
        }
        itemSongTitleLabel.text = title

        itemView.findViewById<ImageButton>(R.id.itemSongMoreButton).setOnClickListener {
            onMore?.invoke(item)
        }
        return itemView
    }
}