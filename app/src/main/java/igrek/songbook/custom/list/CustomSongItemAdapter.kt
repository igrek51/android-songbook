package igrek.songbook.custom.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.layout.list.ListItemClickListener
import igrek.songbook.persistence.general.model.Song
import igrek.songbook.persistence.user.custom.CustomCategory
import java.util.*

class CustomSongItemAdapter internal constructor(
        context: Context,
        _dataSource: List<CustomSongListItem>?,
        private val onClickListener: ListItemClickListener<CustomSongListItem>
) : ArrayAdapter<CustomSongListItem>(context, 0, ArrayList()) {

    var dataSource: List<CustomSongListItem>? = null
    private val inflater: LayoutInflater

    init {
        var dataSource = _dataSource
        if (dataSource == null)
            dataSource = ArrayList()
        this.dataSource = dataSource
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItem(position: Int): CustomSongListItem? {
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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = dataSource!![position]

        return when {
            item.customCategory != null -> createCategoryView(item.customCategory, parent)
            item.song != null -> createSongView(item, item.song, parent)
            else -> View(context)
        }
    }

    private fun createCategoryView(customCategory: CustomCategory, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.list_item_custom_category, parent, false)
        val itemTitleLabel = itemView.findViewById<TextView>(R.id.itemTitleLabel)
        itemTitleLabel.text = customCategory.name

        return itemView
    }

    private fun createSongView(item: CustomSongListItem, song: Song, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.list_item_custom_song, parent, false)
        val itemTitleLabel = itemView.findViewById<TextView>(R.id.itemTitleLabel)
        itemTitleLabel.text = song.displayName()

        val itemMoreButton = itemView.findViewById<ImageButton>(R.id.itemSongMoreButton)
        itemMoreButton.setOnClickListener { onClickListener.onMoreActions(item) }

        return itemView
    }
}