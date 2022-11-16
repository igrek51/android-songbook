package igrek.songbook.playlist.list

import android.content.Context
import android.widget.AbsListView

class TreeListScrollHandler(private val listView: PlaylistListView, context: Context) :
    AbsListView.OnScrollListener {

    var scrollOffset: Int? = 0
        private set

    private var scrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE

    private val smoothScrollEdgeDp = 200
    private val smoothScrollEdgePx: Int
    private val smoothScrollFactor = 0.34f
    private val smoothScrollDuration = 10

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
        smoothScrollEdgePx = (smoothScrollEdgeDp / metrics.density).toInt()
    }

    fun handleScrolling(): Boolean {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            val offset = listView.computeVerticalScrollOffset()
            val height = listView.height
            val extent = listView.computeVerticalScrollExtent()
            val range = listView.computeVerticalScrollRange()

            if (listView.reorder?.isDragging == true
                && listView.reorder.hoverBitmapBounds != null
            ) {
                val hoverViewTop = listView.reorder.hoverBitmapBounds!!.top
                val hoverHeight = listView.reorder.hoverBitmapBounds!!.height()

                if (hoverViewTop <= smoothScrollEdgePx && offset > 0) {
                    val scrollDistance =
                        ((hoverViewTop - smoothScrollEdgePx) * smoothScrollFactor).toInt()
                    listView.smoothScrollBy(scrollDistance, smoothScrollDuration)
                    return true
                }
                if (hoverViewTop + hoverHeight >= height - smoothScrollEdgePx && offset + extent < range) {
                    val scrollDistance =
                        ((hoverViewTop + hoverHeight - height + smoothScrollEdgePx) * smoothScrollFactor).toInt()
                    listView.smoothScrollBy(scrollDistance, smoothScrollDuration)
                    return true
                }
            }
        }
        return false
    }

    override fun onScroll(
        view: AbsListView,
        firstVisibleItem: Int,
        visibleItemCount: Int,
        totalItemCount: Int
    ) {
        scrollOffset = realScrollPosition
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        this.scrollState = scrollState
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            if (listView.reorder?.isDragging == true) {
                val scrollingResult = handleScrolling()
                if (!scrollingResult) {
                    listView.reorder.handleItemDragging()
                }
            }
        }
    }

}
