package igrek.songbook.layout.list

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ListView

abstract class GenericListView<T>(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.listViewStyle,
        val itemViewRes: Int,
) : ListView(context, attrs, defStyleAttr), AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private var adapter: GenericListAdapter<T> = GenericListAdapter(
            context,
            dataSource = emptyList(),
            layoutRes = itemViewRes,
            viewBuilder = ::buildView,
    )

    var items: List<T>
        get() = adapter.dataSource
        set(items) {
            adapter.run {
                dataSource = items
                invalidate()
                notifyDataSetChanged()
            }
            invalidate()
        }

    abstract fun buildView(view: View, item: T)

    abstract fun onClick(item: T)

    init {
        onItemClickListener = this
        onItemLongClickListener = this
        choiceMode = CHOICE_MODE_SINGLE

        setAdapter(adapter)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val item = adapter.getItem(position)
        if (item != null)
            onClick(item)
    }

    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        val item = adapter.getItem(position)
        if (item != null)
            onClick(item)
        return true
    }

    fun add(element: T) {
        items = items + element
    }

    fun enableNestedScrolling() {
        // enable scrolling inside scrollview
        this.setOnTouchListener { v, event ->
            when (event.action) {
                // Disallow ScrollView to intercept touch events.
                MotionEvent.ACTION_DOWN -> v.parent.requestDisallowInterceptTouchEvent(true)
                // Allow ScrollView to intercept touch events.
                MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
            }
            // Handle ListView touch events.
            v.onTouchEvent(event)
            true
        }
    }

    fun scrollToBottom() {
        this.setSelection(this.count - 1)
    }
}
