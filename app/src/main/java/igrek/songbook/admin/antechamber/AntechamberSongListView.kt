package igrek.songbook.admin.antechamber

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import igrek.songbook.songselection.ListScrollPosition

class AntechamberSongListView : ListView, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private var itemAdapter: AntechamberSongListAdapter? = null
    private var onClick: ((AntechamberSong) -> Unit)? = null
    private var onLongClick: ((AntechamberSong) -> Unit)? = null

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

    fun init(context: Context,
             onClick: (item: AntechamberSong) -> Unit,
             onLongClick: (item: AntechamberSong) -> Unit,
             onMore: (item: AntechamberSong) -> Unit
    ) {
        this.onClick = onClick
        this.onLongClick = onLongClick
        onItemClickListener = this
        onItemLongClickListener = this
        choiceMode = CHOICE_MODE_SINGLE
        itemAdapter = AntechamberSongListAdapter(context, null)
        itemAdapter!!.setOnMoreListener(onMore)
        adapter = itemAdapter
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val item = itemAdapter!!.getItem(position)
        onClick?.invoke(item!!)
    }

    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        val item = itemAdapter!!.getItem(position)
        onLongClick?.invoke(item!!)
        return true
    }

    fun setItems(items: List<AntechamberSong>) {
        itemAdapter!!.setDataSource(items)
        invalidate()
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
