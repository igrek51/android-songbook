package igrek.songbook.playlist.list

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import igrek.songbook.layout.list.ListItemClickListener
import igrek.songbook.songselection.ListScrollPosition

class PlaylistListView : ListView, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private var adapter: PlaylistListItemAdapter? = null
    private var onClickListener: ListItemClickListener<PlaylistListItem>? = null

    val currentScrollPosition: ListScrollPosition
        get() {
            var yOffset = 0
            if (childCount > 0) {
                yOffset = -getChildAt(0).top
            }
            return ListScrollPosition(firstVisiblePosition, yOffset)
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun init(context: Context, onClickListener: ListItemClickListener<PlaylistListItem>) {
        this.onClickListener = onClickListener
        onItemClickListener = this
        onItemLongClickListener = this
        choiceMode = CHOICE_MODE_SINGLE
        adapter = PlaylistListItemAdapter(context, null, onClickListener)
        setAdapter(adapter)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val item = adapter!!.getItem(position)
        if (onClickListener != null)
            onClickListener!!.onItemClick(item!!)
    }

    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        val item = adapter!!.getItem(position)
        if (onClickListener != null)
            onClickListener!!.onItemLongClick(item!!)
        return true
    }

    fun setItems(items: List<PlaylistListItem>) {
        adapter!!.setDataSource(items)
        invalidate()
    }

    /**
     * @param position of element to scroll
     */
    private fun scrollTo(position: Int) {
        setSelection(position)
        invalidate()
    }

    fun scrollToBeginning() {
        scrollTo(0)
    }

    fun restoreScrollPosition(scrollPosition: ListScrollPosition?) {
        if (scrollPosition != null) {
            // scroll to first position
            setSelection(scrollPosition.firstVisiblePosition)
            // and move a little by y offset
            smoothScrollBy(scrollPosition.yOffsetPx, 50)
            invalidate()
        }
    }
}
