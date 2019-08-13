package igrek.songbook.playlist.list

import android.annotation.SuppressLint
import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.MotionEvent
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
        private val onClickListener: ListItemClickListener<PlaylistListItem>,
        private val listView: PlaylistListView
) : ArrayAdapter<PlaylistListItem>(context, 0, ArrayList()) {

    var dataSource: List<PlaylistListItem>? = null
    private val storedViews = SparseArray<View>()
    private val inflater: LayoutInflater

    init {
        var dataSource = _dataSource
        if (dataSource == null)
            dataSource = ArrayList()
        this.dataSource = dataSource
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
        if (storedViews.get(position) != null)
            return storedViews.get(position)

        val item = dataSource!![position]

        val itemView = when {
            item.playlist != null -> createPlaylistView(item, item.playlist, parent)
            item.song != null -> createSongView(item, item.song, parent, position)
            else -> return View(context)
        }

        storedViews.put(position, itemView)

        return itemView
    }

    private fun createPlaylistView(item: PlaylistListItem, playlist: Playlist, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.playlist_group_item, parent, false)
        val itemTitleLabel = itemView.findViewById<TextView>(R.id.itemTitleLabel)
        itemTitleLabel.text = playlist.name

        val itemMoreButton = itemView.findViewById<ImageButton>(R.id.itemMoreButton)
        itemMoreButton.setOnClickListener { onClickListener.onMoreActions(item) }
        return itemView
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createSongView(item: PlaylistListItem, song: Song, parent: ViewGroup, position: Int): View {
        val itemView = inflater.inflate(R.layout.playlist_song_item, parent, false)
        val itemTitleLabel = itemView.findViewById<TextView>(R.id.itemTitleLabel)
        itemTitleLabel.text = song.displayName()

        val itemMoreButton = itemView.findViewById<ImageButton>(R.id.itemMoreButton)
        itemMoreButton.setOnClickListener { onClickListener.onMoreActions(item) }

        val moveButton = itemView.findViewById<ImageButton>(R.id.itemMoveButton)
        moveButton.isFocusableInTouchMode = false
        moveButton.isFocusable = false
        moveButton.isClickable = false
        moveButton.setOnTouchListener { _, event ->
            event.source = 777 // from moveButton
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    listView.reorder
                            ?.onItemMoveButtonPressed(position, itemView, event.y + moveButton.top)
                    return@setOnTouchListener false
                }
                MotionEvent.ACTION_MOVE ->
                    return@setOnTouchListener false
                MotionEvent.ACTION_UP -> {
                    listView.reorder
                            ?.onItemMoveButtonReleased()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }

        return itemView
    }

    fun getStoredView(position: Int): View? {
        return if (position >= dataSource!!.size) null else storedViews.get(position)
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    fun invalidate() {
        storedViews.clear()
    }
}