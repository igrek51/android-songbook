package igrek.songbook.songselection.listview

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import igrek.songbook.R
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.appFactory
import igrek.songbook.playlist.PlaylistFillItem
import igrek.songbook.playlist.PlaylistService
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.search.SongSearchItem
import igrek.songbook.songselection.tree.SongTreeItem
import io.reactivex.android.schedulers.AndroidSchedulers

class SongItemViewFactory(
    private val inflater: LayoutInflater,
    private val songContextMenuBuilder: SongContextMenuBuilder,
) {

    fun createView(item: SongTreeItem, parent: ViewGroup): View {
        if (item is SongSearchItem) {
            return createTitleArtistSongView(item, parent)
        }
        if (item is PlaylistFillItem) {
            return createPlaylistFillItemView(item, parent)
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

    @SuppressLint("CheckResult")
    private fun createPlaylistFillItemView(item: PlaylistFillItem, parent: ViewGroup): View {
        val playlistService: PlaylistService = appFactory.playlistService.get()
        val song = item.song!!

        val itemView = inflater.inflate(R.layout.list_item_playlist_fill, parent, false)
        val itemSongTitleLabel = itemView.findViewById<TextView>(R.id.itemSongTitleLabel)

        itemSongTitleLabel.text = song.displayName()

        val itemPlaylistAddSongButton = itemView.findViewById<ImageButton>(R.id.itemPlaylistAddSongButton)
        if (playlistService.isSongOnCurrentPlaylist(song)) {
            itemPlaylistAddSongButton.visibility = View.GONE
        } else {
            itemPlaylistAddSongButton.visibility = View.VISIBLE
            itemPlaylistAddSongButton.setOnClickListener {
                playlistService.addSongToCurrentPlaylist(song)
            }

            playlistService.addPlaylistSongSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (song == it && itemPlaylistAddSongButton.isVisible) {
                        itemPlaylistAddSongButton.visibility = View.GONE
                    }
                }, UiErrorHandler::handleError)
        }
        return itemView
    }
}