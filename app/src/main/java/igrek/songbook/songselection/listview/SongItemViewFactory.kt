package igrek.songbook.songselection.listview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.search.SongSearchItem
import igrek.songbook.songselection.tree.SongTreeItem

class SongItemViewFactory(
        private val inflater: LayoutInflater,
        private val songContextMenuBuilder: SongContextMenuBuilder,
) {

    fun createView(item: SongTreeItem, parent: ViewGroup): View {
        if (item is SongSearchItem) {
            return createTitleArtistSongView(item, parent)
        }
        return if (item.isCategory) {
            createTreeCategoryView(item, parent)
        } else {
            if (item.song!!.isCustom()) {
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

        itemSongTitleLabel.text = item.song?.displayName().orEmpty()

        if (item.song?.isCustom() == true) {
            itemView.findViewById<ImageView>(R.id.songImage)?.run {
                this.setBackgroundResource(R.drawable.edit)
            }
        }

        val itemSongEditButton = itemView.findViewById<ImageButton>(R.id.itemSongMoreButton)
        itemSongEditButton.setOnClickListener { songContextMenuBuilder.showSongActions(item.song!!) }
        return itemView
    }
}