package igrek.songbook.playlist.treelist

import android.content.Context
import android.graphics.Rect
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RelativeLayout

import java.util.ArrayList

import igrek.todotree.R
import igrek.todotree.commands.ItemEditorCommand
import igrek.todotree.commands.ItemSelectionCommand
import igrek.todotree.domain.treeitem.AbstractTreeItem
import igrek.todotree.domain.treeitem.LinkTreeItem
import igrek.todotree.ui.errorcheck.SafeClickListener

internal class TreeItemAdapter(context: Context, dataSource: List<AbstractTreeItem>?, private val listView: TreeListView) : ArrayAdapter<AbstractTreeItem>(context, 0, ArrayList<E>()) {

    var items: List<AbstractTreeItem>? = null
        private set
    private var selections: Set<Int>? = null // selected indexes
    private val storedViews: SparseArray<View>
    private val inflater: LayoutInflater

    init {
        var dataSource = dataSource
        if (dataSource == null)
            dataSource = ArrayList<AbstractTreeItem>()
        this.items = dataSource
        storedViews = SparseArray()
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setDataSource(dataSource: List<AbstractTreeItem>) {
        this.items = dataSource
        storedViews.clear()
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): AbstractTreeItem? {
        return items!![position]
    }

    fun setSelections(selections: Set<Int>) {
        this.selections = selections
    }

    fun getStoredView(position: Int): View? {
        return if (position >= items!!.size) null else storedViews.get(position)
    }

    override fun getCount(): Int {
        return items!!.size + 1
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getItemId(position: Int): Long {
        if (position < 0)
            return -1
        return if (position >= items!!.size) -1 else position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return if (position == items!!.size) {
            getAddItemView(position, parent)
        } else {
            getItemView(position, parent)
        }
    }

    private fun getItemView(position: Int, parent: ViewGroup): View {
        // get from cache
        if (storedViews.get(position) != null)
            return storedViews.get(position)

        val item = items!![position]
        val itemView: View
        if (!item.isEmpty()) {
            itemView = getParentItemView(item, position, parent)
        } else {
            itemView = getSingleItemView(item, position, parent)
        }

        // store view
        storedViews.put(position, itemView)

        return itemView
    }

    private fun getSingleItemView(item: AbstractTreeItem, position: Int, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.tree_item_single, parent, false)

        //zawartość tekstowa elementu
        val textView = itemView.findViewById(R.id.tvItemContent)
        textView.setText(item.getDisplayName())

        // link
        if (item is LinkTreeItem) {
            val content = SpannableString(item.getDisplayName())
            content.setSpan(UnderlineSpan(), 0, content.length(), 0)
            textView.setText(content)
        }

        //przesuwanie
        val moveButton = itemView.findViewById(R.id.buttonItemMove)
        moveButton.setFocusableInTouchMode(false)
        moveButton.setFocusable(false)
        moveButton.setClickable(false)
        increaseTouchArea(moveButton, 20)
        if (selections == null) {
            moveButton.setOnTouchListener({ v, event ->
                event.setSource(777) // from moveButton
                when (event.getAction()) {
                    MotionEvent.ACTION_DOWN -> {
                        listView.reorder
                                .onItemMoveButtonPressed(position, item, itemView, event.getX(), event
                                        .getY() + moveButton.getTop())
                        return@moveButton.setOnTouchListener false
                    }
                    MotionEvent.ACTION_MOVE -> return@moveButton.setOnTouchListener
                    false
                            MotionEvent . ACTION_UP -> {
                        listView.reorder
                                .onItemMoveButtonReleased(position, item, itemView, event.getX(), event
                                        .getY() + moveButton.getTop())
                        return@moveButton.setOnTouchListener true
                    }
                }
                false
            })
        } else {
            moveButton.setVisibility(View.INVISIBLE)
            moveButton.setLayoutParams(RelativeLayout.LayoutParams(0, 0))
        }

        //checkbox do zaznaczania wielu elementów
        val cbItemSelected = itemView.findViewById(R.id.cbItemSelected)
        cbItemSelected.setFocusableInTouchMode(false)
        cbItemSelected.setFocusable(false)

        if (selections != null) {
            cbItemSelected.setVisibility(View.VISIBLE)
            if (selections!!.contains(position)) {
                cbItemSelected.setChecked(true)
            } else {
                cbItemSelected.setChecked(false)
            }
            cbItemSelected.setOnCheckedChangeListener({ buttonView, isChecked ->
                ItemSelectionCommand()
                        .selectedItemClicked(position, isChecked)
            })
        }

        itemView.setOnTouchListener(TreeItemTouchListener(listView, position))

        return itemView
    }

    private fun getParentItemView(item: AbstractTreeItem, position: Int, parent: ViewGroup): View {
        val itemView = inflater.inflate(R.layout.tree_item_parent, parent, false)

        //zawartość tekstowa elementu
        val textView = itemView.findViewById(R.id.tvItemContent)
        textView.setText(item.getDisplayName())

        // ilość potomków
        val tvItemChildSize = itemView.findViewById(R.id.tvItemChildSize)
        val contentBuilder = "[" + item.size() + "]"
        tvItemChildSize.setText(contentBuilder)

        //edycja elementu
        val editButton = itemView.findViewById(R.id.buttonItemEdit)
        editButton.setFocusableInTouchMode(false)
        editButton.setFocusable(false)
        editButton.setClickable(true)
        increaseTouchArea(editButton, 20)
        if (selections == null && !item.isEmpty()) {
            editButton.setOnClickListener(object : SafeClickListener() {
                fun onClick() {
                    ItemEditorCommand().itemEditClicked(item)
                }
            })
        } else {
            editButton.setVisibility(View.GONE)
        }

        //przesuwanie
        val moveButton = itemView.findViewById(R.id.buttonItemMove)
        moveButton.setFocusableInTouchMode(false)
        moveButton.setFocusable(false)
        moveButton.setClickable(false)
        increaseTouchArea(moveButton, 20)
        if (selections == null) {
            moveButton.setOnTouchListener({ v, event ->
                event.setSource(777) // from moveButton
                when (event.getAction()) {
                    MotionEvent.ACTION_DOWN -> {
                        listView.reorder
                                .onItemMoveButtonPressed(position, item, itemView, event.getX(), event
                                        .getY() + moveButton.getTop())
                        return@moveButton.setOnTouchListener false
                    }
                    MotionEvent.ACTION_MOVE -> return@moveButton.setOnTouchListener
                    false
                            MotionEvent . ACTION_UP -> {
                        listView.reorder
                                .onItemMoveButtonReleased(position, item, itemView, event.getX(), event
                                        .getY() + moveButton.getTop())
                        return@moveButton.setOnTouchListener true
                    }
                }
                false
            })
        } else {
            moveButton.setVisibility(View.INVISIBLE)
            moveButton.setLayoutParams(RelativeLayout.LayoutParams(0, 0))
        }

        //checkbox do zaznaczania wielu elementów
        val cbItemSelected = itemView.findViewById(R.id.cbItemSelected)
        cbItemSelected.setFocusableInTouchMode(false)
        cbItemSelected.setFocusable(false)

        if (selections != null) {
            cbItemSelected.setVisibility(View.VISIBLE)
            if (selections!!.contains(position)) {
                cbItemSelected.setChecked(true)
            } else {
                cbItemSelected.setChecked(false)
            }
            cbItemSelected.setOnCheckedChangeListener({ buttonView, isChecked ->
                ItemSelectionCommand()
                        .selectedItemClicked(position, isChecked)
            })
        }

        itemView.setOnTouchListener(TreeItemTouchListener(listView, position))

        return itemView
    }

    private fun getAddItemView(position: Int, parent: ViewGroup): View {
        // plus
        val itemPlus = inflater.inflate(R.layout.item_plus, parent, false)

        val plusButton = itemPlus.findViewById(R.id.buttonAddNewItem)
        plusButton.setFocusableInTouchMode(false)
        plusButton.setFocusable(false)
        plusButton.setOnClickListener(object : SafeClickListener() {
            fun onClick() {
                ItemEditorCommand().addItemClicked()
            }
        })
        // redirect long click to tree list view
        plusButton.setLongClickable(true)
        plusButton.setOnLongClickListener({ v -> listView.onItemLongClick(null, null, position, 0) })

        return itemPlus
    }

    private fun increaseTouchArea(component: View, sidePx: Int) {
        val parent = component.parent as View  // button: the view you want to enlarge hit area
        parent.post {
            val rect = Rect()
            component.getHitRect(rect)
            rect.top -= sidePx    // increase top hit area
            rect.left -= sidePx   // increase left hit area
            rect.bottom += sidePx // increase bottom hit area
            rect.right += sidePx  // increase right hit area
            parent.touchDelegate = TouchDelegate(rect, component)
        }
    }
}