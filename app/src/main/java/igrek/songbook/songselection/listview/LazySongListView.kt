package igrek.songbook.songselection.listview

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListView
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.appFactory
import igrek.songbook.songselection.SongClickListener
import igrek.songbook.songselection.contextmenu.SongContextMenuBuilder
import igrek.songbook.songselection.tree.SongTreeItem
import igrek.songbook.util.limitTo


class LazySongListView : ListView, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AbsListView.OnScrollListener {

    private var adapter: SongListItemAdapter? = null
    private var onClickListener: SongClickListener? = null
    private var allItems: List<SongTreeItem> = emptyList()
    private var renderItemsCount: Int = 0

    private val initialLazyRenderCount = 60
    private val lazyRenderPadding = 20

    private var scrollState: Int = OnScrollListener.SCROLL_STATE_IDLE

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
        itemsCanFocus = true
        isFocusable = true
        this.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP,
                    KeyEvent.KEYCODE_DPAD_DOWN,
                    KeyEvent.KEYCODE_DPAD_LEFT,
                    KeyEvent.KEYCODE_MEDIA_REWIND,
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                    KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD,
                    KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD,
                    KeyEvent.KEYCODE_DPAD_RIGHT,
                    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
                    KeyEvent.KEYCODE_MEDIA_NEXT,
                    KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD,
                    KeyEvent.KEYCODE_MEDIA_STEP_FORWARD -> {
                        return@setOnKeyListener appFactory.systemKeyDispatcher.get().onKeyDown(keyCode)
                    }
                }
            }
            return@setOnKeyListener false
        }
        scrollState = OnScrollListener.SCROLL_STATE_IDLE
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

        val refreshed = recalculateVisibleItems(firstVisiblePosition, lastVisiblePosition - firstVisiblePosition)
        if (!refreshed)
            showSubItems()
    }

    fun restoreScrollPosition(scrollPosition: ListScrollPosition?) {
        if (scrollPosition != null) {
            restoreScrollRepeatedly(scrollPosition, 10)
        }
    }

    private fun attemptRestoreScrollPosition(scrollPosition: ListScrollPosition) {
        recalculateVisibleItems(scrollPosition.firstVisiblePosition, initialLazyRenderCount)
        setSelection(scrollPosition.firstVisiblePosition)
    }

    private fun restoreScrollRepeatedly(scrollPosition: ListScrollPosition, attempts: Int) {
        attemptRestoreScrollPosition(scrollPosition)

        val current = currentScrollPosition
        if (current.firstVisiblePosition != scrollPosition.firstVisiblePosition) {

            if (attempts > 1) {
                Handler(Looper.getMainLooper()).postDelayed({
                    restoreScrollRepeatedly(scrollPosition, attempts - 1)
                }, 100)
            } else {
                logger.warn("scroll restoring failed")
            }
        }
    }

    private fun showSubItems() {
        val visibleItems = allItems.subList(0, renderItemsCount)
        adapter?.setDataSource(visibleItems)
    }

    private fun recalculateVisibleItems(firstVisible: Int, visibleCount: Int): Boolean {
        val lastVisible = firstVisible + visibleCount
        if (lastVisible + lazyRenderPadding >= renderItemsCount) {
            val newCount = (lastVisible + 2 * lazyRenderPadding).limitTo(allItems.size)
            if (newCount > renderItemsCount) {
                renderItemsCount = newCount
                showSubItems()
                return true
            }
        }
        return false
    }

    override fun onScroll(
            view: AbsListView?, firstVisible: Int, visibleCount: Int, totalCount: Int,
    ) {
        if (scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
            recalculateVisibleItems(firstVisible, visibleCount)
        }
    }

    override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
        this.scrollState = scrollState
    }
}
