package igrek.songbook.songselection.listview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListView
import igrek.songbook.songselection.SongClickListener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.tree.SongTreeItem
import igrek.songbook.util.limitTo


class LazySongListView : ListView, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AbsListView.OnScrollListener {

    private var adapter: SongListItemAdapter? = null
    private var onClickListener: SongClickListener? = null
    private var allItems: List<SongTreeItem> = emptyList()
    private var renderItemsCount: Int = 0

    private val initialLazyRenderCount = 30
    private val lazyRenderPadding = 20

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

    fun init(context: Context, onClickListener: SongClickListener, songContextMenuBuilder: SongContextMenuBuilder) {
        this.onClickListener = onClickListener
        onItemClickListener = this
        onItemLongClickListener = this
        choiceMode = CHOICE_MODE_SINGLE
        adapter = SongListItemAdapter(context, emptyList(), songContextMenuBuilder)
        setAdapter(adapter)
        setOnScrollListener(this)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        adapter?.getItem(position)?.let { item ->
            onClickListener?.onSongItemClick(item)
        }
    }

    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        adapter?.getItem(position)?.let { item ->
            onClickListener?.onSongItemLongClick(item)
        }
        return true
    }

    fun setItems(items: List<SongTreeItem>) {
        allItems = items
        renderItemsCount = initialLazyRenderCount.limitTo(allItems.size)
        recalculateVisibleItems(firstVisiblePosition, lastVisiblePosition - firstVisiblePosition)

        showSubItems()
        invalidate()
    }

    fun restoreScrollPosition(scrollPosition: ListScrollPosition?) {
        if (scrollPosition != null) {
            recalculateVisibleItems(scrollPosition.firstVisiblePosition, initialLazyRenderCount)
            setSelection(scrollPosition.firstVisiblePosition)
            smoothScrollBy(scrollPosition.yOffsetPx, 100)
            invalidate()
        }
    }

    private fun showSubItems() {
        val visibleItems = allItems.subList(0, renderItemsCount)
        adapter?.setDataSource(visibleItems)
    }

    private fun recalculateVisibleItems(firstVisible: Int, visibleCount: Int) {
        if (firstVisible + visibleCount + lazyRenderPadding >= renderItemsCount) {
            val newCount = (renderItemsCount + visibleCount + lazyRenderPadding).limitTo(allItems.size)
            if (newCount > renderItemsCount) {
                renderItemsCount = newCount
                showSubItems()
            }
        }
    }

    override fun onScroll(
            view: AbsListView?, firstVisible: Int, visibleCount: Int, totalCount: Int,
    ) {
        recalculateVisibleItems(firstVisible, visibleCount)
    }

    override fun onScrollStateChanged(v: AbsListView?, s: Int) {}
}
