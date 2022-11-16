package igrek.songbook.songselection.listview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import igrek.songbook.songselection.SongClickListener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.tree.SongTreeItem

class SongListView : ListView, AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener {

    private var adapter: SongListItemAdapter? = null
    private var onClickListener: SongClickListener? = null

    val currentScrollPosition: ListScrollPosition
        get() {
            var yOffset = 0
            if (childCount > 0) {
                yOffset = -getChildAt(0).top
            }
            return ListScrollPosition(firstVisiblePosition, yOffset)
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun init(
        context: Context,
        onClickListener: SongClickListener,
        songContextMenuBuilder: SongContextMenuBuilder
    ) {
        this.onClickListener = onClickListener
        onItemClickListener = this
        onItemLongClickListener = this
        choiceMode = CHOICE_MODE_SINGLE
        itemsCanFocus = true
        isFocusable = true
        descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        adapter = SongListItemAdapter(context, emptyList(), songContextMenuBuilder)
        setAdapter(adapter)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val item = adapter!!.getItem(position)
        if (onClickListener != null)
            onClickListener!!.onSongItemClick(item!!)
    }

    override fun onItemLongClick(
        parent: AdapterView<*>,
        view: View,
        position: Int,
        id: Long
    ): Boolean {
        val item = adapter!!.getItem(position)
        if (onClickListener != null)
            onClickListener!!.onSongItemLongClick(item!!)
        return true
    }

    fun setItems(items: List<SongTreeItem>) {
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
