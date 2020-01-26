package igrek.songbook.songselection

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.search.SongSearchItem
import igrek.songbook.songselection.tree.SongTreeItem
import java.util.*
import javax.inject.Inject

class SongListItemAdapter internal constructor(
        context: Context,
        _dataSource: List<SongTreeItem>?
) : ArrayAdapter<SongTreeItem>(context, 0, ArrayList()) {

    private var dataSource: List<SongTreeItem>? = null
    private val inflater: LayoutInflater

    @Inject
    lateinit var songContextMenuBuilder: SongContextMenuBuilder

    init {
        var dataSource = _dataSource
        if (dataSource == null)
            dataSource = ArrayList()
        this.dataSource = dataSource
        DaggerIoc.factoryComponent.inject(this)
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setDataSource(dataSource: List<SongTreeItem>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): SongTreeItem? {
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

        if (item is SongSearchItem) {
            return createTitleArtistSongView(item, parent)
        }
        return if (item.isCategory) {
            createTreeCategoryView(item, parent)
        } else {
            if (item.song!!.custom) {
                createTitleArtistSongView(item, parent)
            } else {
                createTreeSongView(item, parent)
            }
        }
    }

    private fun createTreeCategoryView(item: SongTreeItem, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.list_item_song_tree_category, parent, false)
        val itemCategoryNameLabel = itemView.findViewById<TextView>(R.id.itemCategoryNameLabel)
        // set item title
        itemCategoryNameLabel.text = item.simpleName
        return itemView
    }

    private fun createTreeSongView(item: SongTreeItem, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.list_item_song_tree_song, parent, false)
        val itemSongTitleLabel = itemView.findViewById<TextView>(R.id.itemSongTitleLabel)
        // set item title
        itemSongTitleLabel.text = item.song!!.title

        val itemSongEditButton = itemView.findViewById<ImageButton>(R.id.itemSongMoreButton)
        itemSongEditButton.setOnClickListener { songContextMenuBuilder.showSongActions(item.song!!) }
        return itemView
    }

    private fun createTitleArtistSongView(item: SongTreeItem, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.list_item_song_tree_song, parent, false)
        val itemSongTitleLabel = itemView.findViewById<TextView>(R.id.itemSongTitleLabel)

        itemSongTitleLabel.text = item.song!!.displayName()

        val itemSongEditButton = itemView.findViewById<ImageButton>(R.id.itemSongMoreButton)
        itemSongEditButton.setOnClickListener { songContextMenuBuilder.showSongActions(item.song!!) }
        return itemView
    }
}