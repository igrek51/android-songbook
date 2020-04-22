package igrek.songbook.playlist.list

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ListView
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.layout.list.ListItemClickListener
import igrek.songbook.songselection.listview.ListScrollPosition

class PlaylistListView : ListView, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    var adapter: PlaylistListItemAdapter? = null
    var scrollHandler: TreeListScrollHandler? = null
        private set
    val reorder: TreeListReorder? = TreeListReorder(this)
    private var onClickListener: ListItemClickListener<PlaylistListItem>? = null
    /** view index -> view height  */
    private val itemHeights = SparseIntArray()
    private lateinit var onMove: (Int, Int) -> List<PlaylistListItem>?

    val currentScrollPosition: ListScrollPosition
        get() {
            var yOffset = 0
            if (childCount > 0) {
                yOffset = -getChildAt(0).top
            }
            return ListScrollPosition(firstVisiblePosition, yOffset)
        }

    var items: List<PlaylistListItem>?
        get() = adapter!!.dataSource
        set(items) {
            adapter?.run {
                dataSource = items
                invalidate()
                notifyDataSetChanged()
            }
            invalidate()
            calculateViewHeights()
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun init(context: Context, onClickListener: ListItemClickListener<PlaylistListItem>, onMove: (Int, Int) -> List<PlaylistListItem>?) {
        this.onClickListener = onClickListener
        this.onMove = onMove
        onItemClickListener = this
        onItemLongClickListener = this
        choiceMode = CHOICE_MODE_SINGLE

        scrollHandler = TreeListScrollHandler(this, context)
        setOnScrollListener(scrollHandler)

        adapter = PlaylistListItemAdapter(context, null, onClickListener, this)
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


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.source == 777) { // from moveButton
            if (ev.action == MotionEvent.ACTION_MOVE)
                return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> if (reorder?.isDragging == true) {
                reorder.setLastTouchY(event.y)
                reorder.handleItemDragging()
                return false
            }
            MotionEvent.ACTION_UP -> {
                reorder?.itemDraggingStopped()
            }
            MotionEvent.ACTION_CANCEL -> {
                reorder?.itemDraggingStopped()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun invalidate() {
        super.invalidate()
        if (reorder?.isDragging == true) {
            reorder.setDraggedItemView()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        reorder?.dispatchDraw(canvas)
    }

    private fun calculateViewHeights() {
        // FIXME: for a moment - there's invalidated item heights map
        val observer = this.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                itemHeights.clear()
                this@PlaylistListView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val viewWidth = this@PlaylistListView.width
                if (viewWidth == 0)
                    logger.warn("List view width == 0")

                val measureSpecW = MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY)
                for (i in 0 until adapter!!.count) {
                    val itemView = adapter!!.getView(i, null, this@PlaylistListView)
                    itemView.measure(measureSpecW, MeasureSpec.UNSPECIFIED)
                    itemHeights.put(i, itemView.measuredHeight)
                }

            }
        })
    }

    fun getItemHeight(position: Int): Int {
        return itemHeights.get(position)
    }

    fun putItemHeight(position: Int?, height: Int?) {
        if (height != null)
            itemHeights.put(position!!, height)
        else
            itemHeights.delete(position!!)
    }

    fun getItemView(position: Int): View? {
        return adapter!!.getStoredView(position)
    }

    public override fun computeVerticalScrollOffset(): Int {
        return super.computeVerticalScrollOffset()
    }

    public override fun computeVerticalScrollExtent(): Int {
        return super.computeVerticalScrollExtent()
    }

    public override fun computeVerticalScrollRange(): Int {
        return super.computeVerticalScrollRange()
    }

    fun itemMoved(draggedItemPos: Int, step: Int): List<PlaylistListItem>? {
        return onMove.invoke(draggedItemPos, step)
    }
}
