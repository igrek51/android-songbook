package igrek.songbook.playlist.list

import android.content.Context
import android.os.Handler
import android.widget.AbsListView
import igrek.songbook.info.logger.LoggerFactory

class TreeListScrollHandler(private val listView: PlaylistListView, context: Context) : AbsListView.OnScrollListener {
    private val logger = LoggerFactory.logger

    var scrollOffset: Int? = 0
        private set

    private var scrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE

    private val SMOOTH_SCROLL_EDGE_DP = 200
    private val SMOOTH_SCROLL_EDGE_PX: Int
    private val SMOOTH_SCROLL_FACTOR = 0.34f
    private val SMOOTH_SCROLL_DURATION = 10

    private val realScrollPosition: Int
        get() {
            if (listView.getChildAt(0) == null)
                return 0

            var sumh = 0
            for (i in 0 until listView.firstVisiblePosition) {
                sumh += listView.getItemHeight(i)
            }
            return sumh - listView.getChildAt(0).top
        }

    val currentScrollPosition: Int?
        get() = realScrollPosition

    init {
        val metrics = context.resources.displayMetrics
        SMOOTH_SCROLL_EDGE_PX = (SMOOTH_SCROLL_EDGE_DP / metrics.density).toInt()
    }

    fun handleScrolling(): Boolean {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            val offset = listView.computeVerticalScrollOffset()
            val height = listView.height
            val extent = listView.computeVerticalScrollExtent()
            val range = listView.computeVerticalScrollRange()

            if (listView.reorder.isDragging && listView.reorder
                            .hoverBitmapBounds != null) {
                val hoverViewTop = listView.reorder.hoverBitmapBounds!!.top
                val hoverHeight = listView.reorder.hoverBitmapBounds!!.height()

                if (hoverViewTop <= SMOOTH_SCROLL_EDGE_PX && offset > 0) {
                    val scrollDistance = ((hoverViewTop - SMOOTH_SCROLL_EDGE_PX) * SMOOTH_SCROLL_FACTOR).toInt()
                    listView.smoothScrollBy(scrollDistance, SMOOTH_SCROLL_DURATION)
                    return true
                }
                if (hoverViewTop + hoverHeight >= height - SMOOTH_SCROLL_EDGE_PX && offset + extent < range) {
                    val scrollDistance = ((hoverViewTop + hoverHeight - height + SMOOTH_SCROLL_EDGE_PX) * SMOOTH_SCROLL_FACTOR).toInt()
                    listView.smoothScrollBy(scrollDistance, SMOOTH_SCROLL_DURATION)
                    return true
                }
            }
        }
        return false
    }

    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        scrollOffset = realScrollPosition
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        this.scrollState = scrollState
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (listView.reorder.isDragging) {
                val scrollingResult = handleScrolling()
                // nie da się scrollować - dojechano do końca zakresu
                if (!scrollingResult) {
                    listView.reorder
                            .handleItemDragging() // sprawdzenie, czy elementy powinny zostać przemieszczone
                }
            }
        }
    }

    /**
     * @param itemIndex pozycja elementu do przescrollowania (-1 - ostatni element)
     */
    fun scrollToItem(itemIndex: Int) {
        var itemIndex = itemIndex
        if (itemIndex == -1)
            itemIndex = listView.items.size - 1
        if (itemIndex < 0)
            itemIndex = 0
        listView.setSelection(itemIndex)
        listView.invalidate()
    }

    fun scrollToPosition(_y: Int) {
        var y = _y
        //wyznaczenie najbliższego elementu i przesunięcie względem niego
        try {
            var position = 0
            while (y > listView.getItemHeight(position)) {
                val itemHeight = listView.getItemHeight(position)
                if (itemHeight == 0) {
                    throw RuntimeException("item height = 0, cant scroll to position")
                }
                y -= itemHeight
                position++
            }

            listView.setSelection(position)
            listView.smoothScrollBy(y, 50)
        } catch (e: RuntimeException) {
            val move = y
            Handler().post { listView.smoothScrollBy(move, 50) }
            logger.warn(e.message)
        }

        listView.invalidate()
    }

    fun scrollToBottom() {
        listView.setSelection(listView.items.size)
    }
}
