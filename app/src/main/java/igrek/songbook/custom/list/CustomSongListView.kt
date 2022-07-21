package igrek.songbook.custom.list

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import igrek.songbook.layout.list.ListItemClickListener
import igrek.songbook.songselection.listview.ListScrollPosition

class CustomSongListView : ListView, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private var adapter: CustomSongItemAdapter? = null
    private var onClickListener: ListItemClickListener<CustomSongListItem>? = null

    val currentScrollPosition: ListScrollPosition
        get() {
            var yOffset = 0
            if (childCount > 0) {
                yOffset = -getChildAt(0).top
            }
            return ListScrollPosition(firstVisiblePosition, yOffset)
        }

    var items: List<CustomSongListItem>
        get() = adapter?.dataSource ?: emptyList()
        set(items) {
            adapter?.run {
                dataSource = items
                invalidate()
                notifyDataSetChanged()
            }
            invalidate()
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun init(context: Context, onClickListener: ListItemClickListener<CustomSongListItem>) {
        this.onClickListener = onClickListener
        onItemClickListener = this
        onItemLongClickListener = this
        choiceMode = CHOICE_MODE_SINGLE
        itemsCanFocus = true
        isFocusable = true
        descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS

        adapter = CustomSongItemAdapter(context, null, onClickListener)
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
