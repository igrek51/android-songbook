package igrek.songbook.playlist.list

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
import igrek.songbook.persistence.user.playlist.Playlist
import java.util.*

class PlaylistListItemAdapter internal constructor(
        context: Context,
        _dataSource: List<PlaylistListItem>?,
        val onClickListener: ListItemClickListener<PlaylistListItem>
) : ArrayAdapter<PlaylistListItem>(context, 0, ArrayList()) {

    private var dataSource: List<PlaylistListItem>? = null
    private val inflater: LayoutInflater

    init {
        var dataSource = _dataSource
        if (dataSource == null)
            dataSource = ArrayList()
        this.dataSource = dataSource
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setDataSource(dataSource: List<PlaylistListItem>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): PlaylistListItem? {
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
            item.playlist != null -> createPlaylistView(item, item.playlist, parent)
            item.song != null -> createSongView(item, item.song, parent)
            else -> return View(context)
        }
    }

    private fun createPlaylistView(item: PlaylistListItem, playlist: Playlist, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.playlist_group_item, parent, false)
        val itemTitleLabel = itemView.findViewById<TextView>(R.id.itemTitleLabel)
        itemTitleLabel.text = playlist.name

        val itemMoreButton = itemView.findViewById<ImageButton>(R.id.itemMoreButton)
        itemMoreButton.setOnClickListener { onClickListener.onMoreActions(item) }
        return itemView
    }

    private fun createSongView(item: PlaylistListItem, song: Song, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.playlist_song_item, parent, false)
        val itemTitleLabel = itemView.findViewById<TextView>(R.id.itemTitleLabel)
        itemTitleLabel.text = song.displayName()

        val itemMoreButton = itemView.findViewById<ImageButton>(R.id.itemMoreButton)
        itemMoreButton.setOnClickListener { onClickListener.onMoreActions(item) }
        return itemView
    }
}